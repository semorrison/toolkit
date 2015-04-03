package net.tqft.toolkit.algebra.spiders

import scala.sys.process._
import org.apache.commons.io.IOUtils

import scala.annotation.tailrec

trait Plantri {
  private var plantriPath: String = {
    val probePaths = for ( dir <- List(".", System.getProperty("user.home") + "/bin") ) yield new java.io.File(dir + "/plantri")
    if ( probePaths(0).exists ) probePaths(0).toPath.toString
    else if ( probePaths(1).exists ) probePaths(1).toPath.toString
    else try { "which plantri".!! }
         catch { case e: Exception => "plantri" }
  }
  
  def setPath(path: String): Unit = { plantriPath = path }
  def getPath = plantriPath
}

object Plantri extends Plantri {
  def parseEdgeCodeBytes(rawData: Array[Byte]): Seq[IndexedSeq[IndexedSeq[Int]]] = {
    // Converts plantri binary edge code output, passed as a byte array,
    // into a sequence of graphs given by their edge adjacency lists.
    //
    // Outputs a Seq of IndexedSeqs, each representing a single graph G.
    // The vth element of an IndexedSeq G is the CW sequence of edges coming
    // out of vertex v in G.

    @tailrec def splitGraphSections(pre: IndexedSeq[Int], post: Seq[IndexedSeq[Int]]): Seq[IndexedSeq[Int]] = {
      // Splits input into IndexedSeq sections per graph      
      if (pre.isEmpty) post
      else {
        // The following values depend on the header type of the section; see plantri-guide.txt for details 
        val bodyLength = if (pre.head != 0) pre.head else java.nio.ByteBuffer.wrap(Array(0, 0, pre(2), pre(3)).map(_.toByte)).getInt
        val sectionStartIndex = if (pre.head != 0) 1 else 4

        splitGraphSections(pre.slice(bodyLength + 1, pre.length + 1), pre.slice(sectionStartIndex, sectionStartIndex + bodyLength) +: post)
      }
    }

    def parseGraph(raw: IndexedSeq[Int]): IndexedSeq[IndexedSeq[Int]] = {
      // Convert each graph code section from plantri output format to the input format of edgeAdjListToPlanarGraph
      val iter = raw.toIterator
      return (Iterator continually { iter takeWhile (_ != -1) }
        takeWhile { !_.isEmpty }
        map { _.toIndexedSeq }).toIndexedSeq
    }
    
    return splitGraphSections(rawData.map(_.toInt), Seq()).map(parseGraph(_))
  }
  // Overload to directly read an output file written by plantri
  def parseEdgeCodeBytes(file: String): Seq[IndexedSeq[IndexedSeq[Int]]] = parseEdgeCodeBytes(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(file)))

  def edgeAdjListToPlanarGraph(eAdjs: IndexedSeq[IndexedSeq[Int]]): PlanarGraph = {
    // Input:  the edge adjacency list ("edge code") of a graph G as a
    // IndexedSeq of IndexedSeqs of integers.
    // The IndexedSeq of integers at index v is a CW sequence of edges out of vertex v.
    //
    // NOTES:
    // 0. This code guaranteed safe for duals of disk triangulations output by plantri,
    //    but is not guaranteed to work for all types of plantri output.
    // 
    // 1. In order to correctly get the outerFace parameter for PlanarGraph we require
    //    the first vertex in the edge code to be on the outer face.
    //    plantri does this by default for duals of disk triangulations.
    // 
    // 2. Not guaranteed safe for graphs with loops! Will implement this later if necessary.

    val numOfVertices = eAdjs.length

    // Map edges (: Int) to their endpoints (: IndexedSeq[Int])
    // OPTIMIZATION NOTE: Premature optimization is the root of all evil,
    // but I can't help thinking that using mutable.Set and going through eAdjs once for each vertex
    // and updating the corresponding edge set might be better.
    val vertexPairings = eAdjs.flatten.distinct. // get distinct edges
      map((e: Int) => (e, (for (v <- 0 until numOfVertices if eAdjs(v).contains(e)) yield v))).
      toMap

    // Temporary variable (: IndexedSeq[(IndexedSeq[Int], Array[Int])]) en route to constructing final vertexFlags.
    // Construct IndexedSeq of tuples; the tuple (IndexedSeq, Array) in the v-th entry describes
    // the ACW cyclic ordering of (edge, leftward face) pairings ("flags") around vertex v.
    // The IndexedSeq in the first entry stores the ACW cyclic edge adjacencies, and
    // the array (mutable!) in the second entry stores the leftward face associated to each edge.
    // We will update the arrays with face labels by traversing the graph.
    var tmpVertexFlags = eAdjs.map(L => (L.reverse, Array.fill(L.length)(-1)))

    def traverse(vertex: Int, edgeIndex: Int, faceLabel: Int): Unit = {
      // Take a vertex and
      // *the index in tmpVertexFlags(startVertex)._1 of* an edge going out of it,
      // and traverse the boundary of the face to the left of startEdge,
      // updating the relevant entries in tmpVertexFlags with the face label as we go.
      // Traversal ends when we reach a left turn that has already been traveled. 

      def step(currentVertex: Int, outEdge: Int): (Int, Int) = {
        // Takes the current vertex we're on, and the edge to travel down,
        // and returns a tuple of (the vertex "nextVertex" we travel to, edge "leftTurn" out of nextVertex "to the left" of outEdge)
        val tmpNextVertex = vertexPairings(outEdge).diff(Seq(currentVertex))
        val nextVertex = if (tmpNextVertex.isEmpty) vertexPairings(outEdge).head else tmpNextVertex.head // Need this check to handle loops.
        // (Need more to handle nested loops however.)
        // Take the next edge from outEdge in the CW edge adjacencies for nextVertex 
        val leftTurn = eAdjs(nextVertex)((eAdjs(nextVertex).indexOf(outEdge) + 1) % eAdjs(nextVertex).length) // This needs to be modified to handle loops properly.

        return (nextVertex, leftTurn)
      }

      if (tmpVertexFlags(vertex)._2(edgeIndex) != -1) // If the face is not labeled -1 we've already updated the
        Unit // face label for the edge out of this vertex
      else {
        tmpVertexFlags(vertex)._2(edgeIndex) = faceLabel
        val (v, e) = step(vertex, tmpVertexFlags(vertex)._1(edgeIndex))
        traverse(v, tmpVertexFlags(v)._1.indexOf(e), faceLabel)
      }
    }

    // Traverse graph faces and update tmpVertexFlags
    var face = 0 // face label
    for (v <- 0 until numOfVertices) {
      var untraversedFaceAt = tmpVertexFlags(v)._2.indexOf(-1) // get index of edge for which we have not yet found the left face
      while (untraversedFaceAt != -1) {
        traverse(v, untraversedFaceAt, face)
        face += 1
        untraversedFaceAt = tmpVertexFlags(v)._2.indexOf(-1)
      }
    }
    
    // vertexFlags: IndexedSeq[Seq[(Int, Int)]] describes the half edges coming out of each vertex
    val vertexFlags = for (v <- 0 until numOfVertices) yield (tmpVertexFlags(v)._1.zip(tmpVertexFlags(v)._2))
    val outerFace = vertexFlags(0).head._2
    val labels = 0 until numOfVertices
    val loops = 0 // vertexPairings.values.count(S => S.length == 1) once we correctly implement loop functionality
    
    return PlanarGraph(outerFace, vertexFlags, labels, loops)
  }
  
  def apply(bdryPts: Int, intVertices: Int, verbose: Boolean = false): Seq[PlanarGraph] = {
    // Returns a list of trivalent planar graphs with bdryPts boundary points
    // and intVertices internal vertices.
    require(bdryPts > 2, "Number of boundary points must be > 2.")
    
    var bytes = Array[Byte]()
    var logIt = Iterator[String]()
    val totalVertices = bdryPts + intVertices
    
    val runPlantri = Process(this.getPath + " " + totalVertices + " -P" + bdryPts + " -Edho -c2m2" + { if (verbose) " -v" else "" }) // see plantri-guide.txt for flag info
    val ioHandler = new ProcessIO( os => (),
                                   is => bytes = IOUtils.toByteArray(is),
                                   is => logIt = scala.io.Source.fromInputStream(is).getLines )
    runPlantri.run(ioHandler)
    while (bytes.length == 0 || !logIt.hasNext) Thread.sleep(10) // Ping for result
    
    // (bytes, log) // Debugging
    
    val log = logIt.toList
    if (verbose) { // Verbose mode
      println("Running plantri:")
      log.map(println(_)) // Notify when plantri has finished
      println("\nParsing output...")
    }
    val planarGraphs = parseEdgeCodeBytes(bytes).map(edgeAdjListToPlanarGraph(_))
    
    // Check that we parsed the binary correctly; should have the number of graphs reported by plantri
    require( planarGraphs.length == log(log.length - 1).split(" ")(0).toInt, "Something went wrong parsing the plantri output." )
    
    return planarGraphs
  }
  
  // Check plantri path is correct
  try assert(this.getPath != "plantri")
  catch { case e: AssertionError => this.setPath( scala.io.StdIn.readLine("WARNING: plantri not found. Please set the path:\n") ) }
  
  // Check plantri works
  try assert(apply(4,4).length == 147)
  catch {
    case e: AssertionError => println("ERROR: The copy of plantri at " + this.getPath + " does not appear to be working.")
    case e: java.io.IOException => println("ERROR: plantri not found!")
  }
}