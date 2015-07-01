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


BeginPackage["Spiders`Trivalent`",{"Spiders`"}]


Trivalent;SO3Categories;dMinusOne;SO3;CubicTrivalentCategories;CubicTrivalentCategory;G2Categories;ABACategories


Begin["`Private`"];


Clear[Trivalent]
Trivalent:=Trivalent=Module[{Trivalent},Trivalent={DeclareDimensionBounds[{1,0,1,1}][emptySpiderAnalysis[FreeSpider[{{3,0,1,1}}],{d,t}]]};Trivalent=ConsiderDiagrams[{PlanarGraphs@empty[]}][Trivalent];Trivalent=ConsiderDiagrams[{polygon[1]}][Trivalent];Trivalent=DeclarePolynomialNonZero[d][DeclarePolynomialZero[d-Name[PlanarGraphs@loop[]]][Trivalent]];Trivalent=ConsiderDiagrams[{PlanarGraphs@strand[],polygon[2]}][Trivalent];Trivalent=DeclarePolynomialsZero[{d-Name[PlanarGraphs@theta[]]}][Trivalent];Trivalent=ConsiderDiagrams[{PlanarGraphs@trivalentVertex[],polygon[3]}][Trivalent];Trivalent=DeclarePolynomialsZero[{d t-Name[PlanarGraphs@tetrahedron[]]}][Trivalent];Trivalent
]


(* TODO Construct Fib, and explain why we assume t^2-t-1\[NotEqual]0 in what follows. *)


SO3Categories:=SO3Categories=Module[{SO3Categories},
SO3Categories=DeclarePolynomialNonZero[t^2-t-1][DeclareDimensionBounds[{1,0,1,1,3}][Trivalent]];SO3Categories=ConsiderDiagrams[ReducedDiagrams[SO3Categories[[1]],4,0]~Join~ReducedDiagrams[SO3Categories[[1]],4,2]~Join~{polygon[4],PlanarGraphs@twoSquares[]}][SO3Categories];

If[Length[SO3Categories]!=2,Print["Something went wrong while building the SO(3) categories!"];Abort[]];

(* Now split out the 2 cases carefully *)
{{dMinusOne},{SO3}}={
Cases[SO3Categories,s_/;Length[IndependentDiagrams[4][s]]==3\[And]ReducePolynomials[s][{d+1,2t-3}]==={0,0}],
Cases[SO3Categories,
s_/;Length[IndependentDiagrams[4][s]]==3\[And]ReducePolynomials[s][2t-3]=!=0]
};
{dMinusOne,SO3}
]
dMinusOne:=dMinusOne=SO3Categories[[1]];
SO3:=SO3=SO3Categories[[2]];


CubicTrivalentCategories:=CubicTrivalentCategories=Module[{CubicTrivalentCategories,diagrams},
(* TODO explicit non-zero polynomials no longer necessary? *)
CubicTrivalentCategories=DeclarePolynomialsNonZero[{2-d-t+d t}][DeclareDimensionBounds[{{1,1},{0,0},{1,1},{1,1},{4,4}}][Trivalent]];
CubicTrivalentCategories=DeclarePolynomialEitherZeroOrNonZero[-d^2+d+1][CubicTrivalentCategories];diagrams=ReducedDiagrams[CubicTrivalentCategories[[1]],4,0]~Join~ReducedDiagrams[CubicTrivalentCategories[[1]],4,2]~Join~{polygon[4],PlanarGraphs@twoSquares[]};CubicTrivalentCategories=ConsiderDiagrams[diagrams][CubicTrivalentCategories];

If[Length[CubicTrivalentCategories]!=11,Print["Something went wrong while building the cubic trivalent categories!"];Abort[]];

CubicTrivalentCategories
]


CubicTrivalentCategory:=CubicTrivalentCategory=DeclarePolynomialNonZero[(d^2-1)(d^2-d-1)][CubicTrivalentCategories]


Clear[G2Categories]
G2Categories:=G2Categories=Module[{G2Categories},
(* TODO explicit non-zero polynomials no longer necessary? (Changing the lower bound to 9 *)G2Categories=DeclarePolynomialNonZero[t^2-t-1][DeclareDimensionBounds[{1,0,1,1,4,10}][CubicTrivalentCategory]];
G2Categories=ConsiderDiagrams[ReducedDiagrams[G2Categories[[1]],5,1]~Join~ReducedDiagrams[G2Categories[[1]],5,3]~Join~{polygon[5]}][G2Categories];

G2Categories
]


Clear[ABACategories]
ABACategories:=ABACategories=Module[{ABACategories},ABACategories=DeclareDimensionBounds[{1,0,1,1,4,8}][CubicTrivalentCategory];ABACategories=ConsiderDiagrams[ReducedDiagrams[ABACategories[[1]],5,1]~Join~ReducedDiagrams[ABACategories[[1]],5,3]~Join~{polygon[5]}][ABACategories];

ABACategories
]


End[];


EndPackage[];
