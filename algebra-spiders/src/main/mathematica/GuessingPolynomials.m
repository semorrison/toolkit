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


BeginPackage["GuessingPolynomials`"];


FindPolynomial;FindMultivariablePolynomial;FindVerifiedMultivariablePolynomial;SchwartzZippelDeterminant;SchwartzZippelNullSpace;SchwartzZippelInverse;


Begin["`Private`"];


FindPolynomialStep=1000;


FindPolynomial[v_][f_]:=Module[{n=0,p,result={$Failed},primes={},values={}},
p[n_]:=Prime[1+n FindPolynomialStep];
While[!FreeQ[result,$Failed]\[Or](result/.v->p[n+1])=!=f[p[n+1]],
n++;
AppendTo[primes,p[n]];
AppendTo[values,f[p[n]]];
If[EvenQ[n],
primes=Rest[primes];
values=Rest[values]
];
(*Print[{n,primes,values}];*)
result=FindPolynomial[v,primes][values]
];
result
]


FindPolynomial[v_,primes_][values:{___Integer}]:=Module[{e},
(*Print["FindPolynomial[",v,",",primes,"][",values,"]"];*)
If[Max[Abs[values]]<=10^50,
e=GCD@@Flatten[(FactorInteger/@values)[[All,All,2]]];
FindNonPowerPolynomial[v,primes][Surd[values,e]]^e,
FindNonPowerPolynomial[v,primes][values]
]
]
FindPolynomial[v_,primes_][values:{___Rational}]/;MatchQ[Union[Numerator/@values],{1}|{-1}]:=FindPolynomial[v,primes][values^-1]^-1
FindPolynomial[v_,primes_][values:{___Rational}]:=FindPolynomial[v,primes][Numerator[values]]/FindPolynomial[v,primes][Denominator[values]]


FindNonPowerPolynomial[v_,primes_][{0...}]:=0
FindNonPowerPolynomial[v_,primes_][values:{___Integer}]:=Module[{residues,crt,next},
(* Print[values]; *)
residues=Mod[#[[1]],#[[2]],-#[[2]]/2]&/@Transpose[{values,primes}];
crt=Mod[ChineseRemainder[residues,primes],Times@@primes,-(Times@@primes)/2];
(* Print[residues];
Print[crt]; *)
If[Abs[crt/(Times@@primes)]<1/10,
next=FindNonPowerPolynomial[v,primes][(values-crt)/primes];
If[next===$Failed,
$Failed,
crt+v next
],
$Failed
]
]


FindMultivariablePolynomial[{v_}][f_]:=(*FindPolynomial[v][f]*)f[v]
FindMultivariablePolynomial[variables_][f_]:=Module[{v=First[variables],ev,p,n=0,result=$Failed,primes={},values={}},
(*Print["FindMultivariablePolynomial[",variables,",",k,"][",f,"]"];*)
p[n_]:=Prime[1+n FindPolynomialStep];
ev[n_]:=ev[n]=Factor[FindMultivariablePolynomial[Rest[variables]][f[p[n],##]&]];
While[!FreeQ[{result},$Failed]\[Or]Together[(result/.v->p[n+1])-ev[n+1]]=!=0,
Print[result];
n++;
AppendTo[primes,p[n]];
AppendTo[values,ev[n]];
If[EvenQ[n],
primes=Rest[primes];
values=Rest[values]
];
(*Print[n];
Print[primes];
Print[values];*)
result=FindMultivariablePolynomial[v,primes][values]
];
result
]


FindVerifiedMultivariablePolynomial[degreeBound0_,probability_:10^-50][variables_][f_]:=
Module[{guess=FindMultivariablePolynomial[variables][f],degreeBound,probabilityBound=1,FindPolynomialStep0=FindPolynomialStep,S,bestTiming=\[Infinity],timing,result,p},
degreeBound=degreeBound0+Total[Exponent[Numerator[guess],Variables[guess]]]+Total[Exponent[Denominator[guess],Variables[guess]]];
S=2degreeBound;
While[probabilityBound>probability,
(*Print[{N[probabilityBound],S}];*)
p=Table[RandomInteger[{1,S}],{Length[variables]}];
{timing,result}=AbsoluteTiming[(guess/.(Thread[variables->p]))-(f@@p)===0];
timing=timing/Log[S];
If[result,
probabilityBound=probabilityBound degreeBound / S;
(* update S, based on timing *)
If[timing<bestTiming,
bestTiming=timing;
S*=2
],
FindPolynomialStep*=2;
guess=FindMultivariablePolynomial[variables][f];
degreeBound=degreeBound0+Total[Exponent[Numerator[guess],Variables[guess]]]+Total[Exponent[Denominator[guess],Variables[guess]]];
S=2degreeBound;
probabilityBound=1
];
];
FindPolynomialStep=FindPolynomialStep0;
guess
]


FindMultivariablePolynomial[v_,{}][{}]:=$Failed
FindMultivariablePolynomial[v_,primes_][values:{___Integer}]:=FindPolynomial[v,primes][values]
FindMultivariablePolynomial[v_,primes_][values:{___Times}]:=Module[{},
If[Length[Union[Length/@values]]==1,
Product[FindMultivariablePolynomial[v,primes][values[[All,i]]],{i,1,Length[values[[1]]]}],
FindMultivariablePolynomial[v,Rest[primes]][Rest[values]]
]
]
FindMultivariablePolynomial[v_,primes_][values:{___Power}]:=Module[{},
If[Length[Union[values[[All,2]]]]==1,
FindMultivariablePolynomial[v,primes][values[[All,1]]]^values[[1,2]],
FindMultivariablePolynomial[v,Rest[primes]][Rest[values]]
]
]
FindMultivariablePolynomial[v_,primes_][values_]:=Module[{variables,exponents,coefficients},
variables=Variables[values];
coefficients=CoefficientRules[values,variables];
exponents=Union[Flatten[coefficients[[All,All,1]],1]];
Factor[Sum[FindPolynomial[v,primes][(e/.coefficients)/.(e->0)](Times@@(variables^e)),{e,exponents}]]
]


totalDegree[f_]:=Module[{t=Together[f],v=Variables[f]},(Total[Exponent[Numerator[t],v]])+(Total[Exponent[Denominator[t],v]])
]


SchwartzZippelDeterminant[matrix_,probabilityBound_:10^-50]:=Module[{variables,degreeBound},
variables=Reverse[SortBy[Variables[matrix],Max[Exponent[matrix,#]]&]];
degreeBound=Total[Max/@Map[totalDegree,matrix,{2}]];
FindVerifiedMultivariablePolynomial[degreeBound,probabilityBound][variables][(Print["evaluating at ",{##}];Det[matrix/.Thread[variables->{##}]])&]
]



SchwartzZippelInverse[matrix_,probabilityBound_:10^-50]:=Module[{variables,degreeBound,det,inverse},
variables=Reverse[SortBy[Variables[matrix],Max[Exponent[matrix,#]]&]];
degreeBound=2Total[Max/@Map[totalDegree,matrix,{2}]];
inverse[v___]:=inverse[v]=(Print[DateString[]<> " calculating inverse at ",{v}];
Inverse[matrix/.Thread[variables->{v}],Method->"OneStepRowReduction"]);
inverse[v___]:=inverse[v]=(Print[DateString[]<> " calculating inverse at ",{v}];
onDiskParallelInverse[matrix/.Thread[variables->{v}],"inverse"]);
Table[FindVerifiedMultivariablePolynomial[degreeBound,probabilityBound][variables][inverse[##][[i,j]]&],{i,1,Length[matrix]},{j,1,Length[matrix]}]
]


echo[x_]:=(Print[x];x)


SchwartzZippelNullSpace[matrix_,probabilityBound_:10^-50]:=Module[{e,m,v},
e=SchwartzZippelDeterminant[Drop[matrix,{},{-1}],probabilityBound];
m=ReplacePart[matrix,1->Factor[matrix[[1]]/e]];
v=Table[echo[(-1)^k SchwartzZippelDeterminant[Drop[m,{},{k}],probabilityBound]],{k,1,Length[matrix[[1]]]}];
Factor[v/v[[-1]]]
]


inMemoryParallelRowReduce[matrix_]:=Module[{m,load,save},
load[j_]:=m[[j]];
save[j_,row_]:=m[[j]]=row;
m=matrix;
SetSharedVariable[m];
DistributeDefinitions[load,save];
parallelRowReduce[Length[matrix],1,load,save]
]


onDiskParallelRowReduce[matrix_,namer_]:=Module[{load,save},
load[j_]:=Get[namer[j]];
save[j_,row_]:=Put[row,namer[j]];
Do[save[j,matrix[[j]]],{j,1,Length[matrix]}];
DistributeDefinitions[load,save];
parallelRowReduce[Length[matrix],1,load,save]
]
onDiskParallelRowReduce[matrix_,tag_String]:=
With[{nbd=NotebookDirectory[]},onDiskParallelRowReduce[matrix,FileNameJoin[{nbd,"matrices",tag<>"-"<>ToString[#]<>".m"}]&]
]


onDiskParallelInverse[matrix_,f_]:=Module[{augmented,s,almost},
augmented=ArrayFlatten[{{matrix,IdentityMatrix[Length[matrix]]}}];
s=Reverse[Range[Length[matrix]]]~Join~(Range[Length[matrix]]+Length[matrix]);
almost=Reverse[onDiskParallelRowReduce[augmented,f]][[All,s]];
almost=Reverse[onDiskParallelRowReduce[almost,f]][[All,s]];
Table[Together[almost[[k,Length[matrix]+1;;]]/almost[[k,k]]],{k,1,Length[matrix]}]
]


parallelRowReduce[n_Integer,k_Integer,load_,save_]:=
Module[{row,pivots},
If[k>n,
Table[load[j],{j,1,n}],
Print[DateString[]," row reducing at row ",k];
row=load[k];
If[row[[k]]===0,
(* we need to pivot first *)
pivots=Cases[Range[k+1,n],j_/;load[j][[k]]=!=0,1,1];
If[Length[pivots]==0,
(* proceed, no need to pivot *)
parallelRowReduce[n,k+1,load,save],
save[k,load[pivots[[1]]]];
save[pivots[[1]],row];
parallelRowReduce[n,k,load,save]],
(* actually do the row reduction *)
ParallelDo[
Module[{r1,r2},
r1=load[k];
r2=load[j];
save[j,Together[r2-r2[[k]]/r1[[k]] r1]]
]
,{j,k+1,n}];
parallelRowReduce[n,k+1,load,save]
]
]
]


End[];


EndPackage[];



