(* ::Package:: *)

(************************************************************************)
(* This file was generated automatically by the Mathematica front end.  *)
(* It contains Initialization cells from a Notebook file, which         *)
(* typically will have the same name as this file except ending in      *)
(* ".nb" instead of ".m".                                               *)
(*                                                                      *)
(* This file is intended to be loaded into the Mathematica kernel using *)
(* the package loading commands Get or Needs.  Doing so is equivalent   *)
(* to using the Evaluate Initialization Cells menu command in the front *)
(* end.                                                                 *)
(*                                                                      *)
(* DO NOT EDIT THIS FILE.  This entire file is regenerated              *)
(* automatically each time the parent Notebook file is saved in the     *)
(* Mathematica front end.  Any changes you make to this file will be    *)
(* overwritten.                                                         *)
(************************************************************************)



SetOptions[InputNotebook[],AutoGeneratedPackage->Automatic]


BeginPackage["Spiders`BraidedTrivalent`",{"Spiders`"}]


BraidedTrivalent;SymmetricTrivalent;BraidedSO3Categories;CubicBraidedTrivalentCategories;CubicBraidedTrivalent;BraidedG2Categories


h;t;d;z


Begin["`Private`"];


Clear[BraidedTrivalent]
BraidedTrivalent:=BraidedTrivalent=Module[{BraidedTrivalent},BraidedTrivalent=DeclareDimensionBounds[{1,0,1,1}][emptySpiderAnalysis[FreeSpider[{{4,0,2,1},{3,0,1,1}}],{h,t,d,z}]];
(* 0-boundary points *)
BraidedTrivalent=ConsiderDiagrams[{PlanarGraphs@empty[]}][BraidedTrivalent];
(* 1-boundary point *)
BraidedTrivalent=ConsiderDiagrams[{polygon[1]}][BraidedTrivalent];
(* 2-boundary points *)
BraidedTrivalent=DeclarePolynomialNonZero[d][DeclarePolynomialZero[d-Name[PlanarGraphs@loop[]]][BraidedTrivalent]];
BraidedTrivalent=DeclarePolynomialsZero[{d z-Name[PlanarGraphs@positiveTwistedTheta[]]}][BraidedTrivalent];BraidedTrivalent=DeclarePolynomialsZero[{d-Name[PlanarGraphs@theta[]]}][BraidedTrivalent];BraidedTrivalent=ConsiderDiagrams[{PlanarGraphs@strand[],PlanarGraphs@positiveTwist[],PlanarGraphs@negativeTwist[],polygon[2],PlanarGraphs@hopfStrand[]}][BraidedTrivalent];(* 3-boundary points *)
BraidedTrivalent=DeclarePolynomialsZero[{d t-Name[PlanarGraphs@tetrahedron[]]}][BraidedTrivalent];
BraidedTrivalent=ConsiderDiagrams[{PlanarGraphs@trivalentVertex[],polygon[3],PlanarGraphs@positiveTwistedTrivalentVertex[],PlanarGraphs@negativeTwistedTrivalentVertex[]}][BraidedTrivalent];(* 4-boundary points *)
BraidedTrivalent=IntroduceRelation[Transpose[{{-1,1},FromScalaObject[PlanarGraphs@Reidemeister2[],1]}]][BraidedTrivalent];BraidedTrivalent=PairRelationsWith[ReducedDiagrams[BraidedTrivalent[[1]],4,0,0]~Join~ReducedDiagrams[BraidedTrivalent[[1]],4,0,2]][BraidedTrivalent];
BraidedTrivalent=DeclarePolynomialsZero[{h-Name[PlanarGraphs@hopfLink[]]}][BraidedTrivalent];
BraidedTrivalent=DeclarePolynomialEitherZeroOrNonZero[z][BraidedTrivalent];
(* 5-boundary points *)
BraidedTrivalent=IntroduceRelation[Transpose[{{-1,1},FromScalaObject[PlanarGraphs@Reidemeister4a[],1]}]][BraidedTrivalent];BraidedTrivalent=IntroduceRelation[Transpose[{{-1,1},FromScalaObject[PlanarGraphs@Reidemeister4b[],1]}]][BraidedTrivalent];
BraidedTrivalent
]


SymmetricTrivalent:=SymmetricTrivalent=Module[{SymmetricTrivalent},
SymmetricTrivalent=IntroduceRelation[{{-1,PlanarGraphs@crossing[]},{1,PlanarGraphs@inverseCrossing[]}}][BraidedTrivalent];
SymmetricTrivalent=PairRelationsWith[ReducedDiagrams[SymmetricTrivalent[[1]],4,0,0]~Join~ReducedDiagrams[SymmetricTrivalent[[1]],4,0,2]][SymmetricTrivalent];
SymmetricTrivalent
]


BraidedSO3Categories:=BraidedSO3Categories=Module[{BraidedSO3Categories},
BraidedSO3Categories=DeclarePolynomialNonZero[t^2-t-1][DeclareDimensionBounds[{1,0,1,1,3}][BraidedTrivalent]];
BraidedSO3Categories=ConsiderDiagrams[ReducedDiagrams[BraidedSO3Categories[[1]],4,0,0]~Join~ReducedDiagrams[BraidedSO3Categories[[1]],4,0,2]~Join~{polygon[4],PlanarGraphs@crossing[]}][BraidedSO3Categories];
BraidedSO3Categories
]


CubicBraidedTrivalentCategories:=CubicBraidedTrivalentCategories=Module[{CubicBraidedTrivalentCategories},
(* TODO explicit non-zero polynomials no longer necessary? *)
CubicBraidedTrivalentCategories=DeclarePolynomialsNonZero[{2-d-t+d t,d+t+d t}][DeclareDimensionBounds[{{1,1},{0,0},{1,1},{1,1},{4,4}}][BraidedTrivalent]];CubicBraidedTrivalentCategories=ConsiderDiagrams[ReducedDiagrams[CubicBraidedTrivalentCategories[[1]],4,0,0]~Join~ReducedDiagrams[CubicBraidedTrivalentCategories[[1]],4,0,2]~Join~{polygon[4],PlanarGraphs@crossing[]}][CubicBraidedTrivalentCategories];CubicBraidedTrivalentCategories
]


CubicBraidedTrivalent:=CubicBraidedTrivalent=Cases[CubicBraidedTrivalentCategories,s_/;s[[2,2]]==={-1-t+t^2+2 z-2 d z-2 t z-z^2+t z^2-d^2 t z^2+2 z^3+2 d t z^3-4 z^4+4 d^2 z^4-d^3 z^4+2 d t z^4-3 d t^2 z^4+d^3 t^2 z^4+2 z^5+2 d t z^5-z^6+t z^6-d^2 t z^6+2 z^7-2 d z^7-2 t z^7-z^8-t z^8+t^2 z^8}]


BraidedG2Categories:=BraidedG2Categories=Module[{BraidedG2},
BraidedG2Categories=DeclarePolynomialNonZero[t^2-t-1][DeclareDimensionBounds[{1,0,1,1,4,10}][CubicBraidedTrivalent]];
BraidedG2Categories=ConsiderDiagrams[ReducedDiagrams[BraidedG2Categories[[1]],5,0,1]~Join~ReducedDiagrams[BraidedG2Categories[[1]],5,0,3]~Join~{polygon[5]}][BraidedG2Categories];
BraidedG2Categories
]


End[];


EndPackage[];
