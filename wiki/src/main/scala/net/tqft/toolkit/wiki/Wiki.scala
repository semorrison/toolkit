package net.tqft.toolkit.wiki

object Wiki extends App {

//  val b = WikiMap("http://tqft.net/mlp/index.php")
//  b.login("arxivbot", "zytopex")
//  b.enableSQLReads("jdbc:mysql://mysql.tqft.net/mathematicsliteratureproject?user=readonly1&password=readonly", "mlp_")
//  b.setThrottle(5000)
//  b("Sandbox") = "test"

  val b = WikiMap("http://katlas.org/w/index.php")
  b.login("Scott", "umozY1")
  b.setThrottle(5000)
  b("Sandbox") = "Kilroy was here"
  
}