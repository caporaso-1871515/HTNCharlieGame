;;; PROBLEMA CHARLIE: LIVELLO 3 (DIFFICILE)
;;; File: charlie-level3-hard.pddl
;;; STRIPS puro
;;;
;;; Scenario Iniziale:
;;;   Charlie in pos13 a quota h3 con mani libere (HandsEmpty).
;;;   Scala a terra in pos2 a quota h2.
;;;   Banana appesa in pos8 a quota h4 (raggiungibile ad altezza h3).
;;;   Profilo terreno completo [pos0..pos20], quote [h0..h5].
;;;   Allineamento colonna pos20: quota h4 (4 scatole).

(define (problem charlie-level3-hard)
  (:domain charlie-strips)

  (:objects
    pos0  pos1  pos2  pos3  pos4  pos5  pos6  pos7  pos8  pos9
    pos10 pos11 pos12 pos13 pos14 pos15 pos16 pos17 pos18 pos19 pos20
    - position

    h0 h1 h2 h3 h4 h5
    - height
  )

  (:init
    ;; Topologia orizzontale (NextX)
    (NextX pos0  pos1)  (NextX pos1  pos2)  (NextX pos2  pos3)
    (NextX pos3  pos4)  (NextX pos4  pos5)  (NextX pos5  pos6)
    (NextX pos6  pos7)  (NextX pos7  pos8)  (NextX pos8  pos9)
    (NextX pos9  pos10) (NextX pos10 pos11) (NextX pos11 pos12)
    (NextX pos12 pos13) (NextX pos13 pos14) (NextX pos14 pos15)
    (NextX pos15 pos16) (NextX pos16 pos17) (NextX pos17 pos18)
    (NextX pos18 pos19) (NextX pos19 pos20)

    ;; Relazioni verticali di adiacenza (NextH e Below)
    (NextH h0 h1) (NextH h1 h2) (NextH h2 h3) (NextH h3 h4) (NextH h4 h5)

    (Below h1 h0) (Below h2 h1) (Below h3 h2) (Below h4 h3) (Below h5 h4)

    ;; Dislivello calpestabile |Δh| <= 1 (WalkableDiff)
    (WalkableDiff h0 h0) (WalkableDiff h1 h1) (WalkableDiff h2 h2)
    (WalkableDiff h3 h3) (WalkableDiff h4 h4) (WalkableDiff h5 h5)

    (WalkableDiff h0 h1) (WalkableDiff h1 h2) (WalkableDiff h2 h3)
    (WalkableDiff h3 h4) (WalkableDiff h4 h5)

    (WalkableDiff h1 h0) (WalkableDiff h2 h1) (WalkableDiff h3 h2)
    (WalkableDiff h4 h3) (WalkableDiff h5 h4)

    ;; Stessa quota (SameH)
    (SameH h0 h0) (SameH h1 h1) (SameH h2 h2) (SameH h3 h3) (SameH h4 h4) (SameH h5 h5)

    ;; Dislivello da scala Δh = 2 (LadderDiff)
    (LadderDiff h0 h2) (LadderDiff h1 h3) (LadderDiff h2 h4) (LadderDiff h3 h5)

    ;; Spazio libero per gravità alla quota di spinta: hDest <= h (CanReceiveBox)
    (CanReceiveBox h0 h0) (CanReceiveBox h0 h1) (CanReceiveBox h0 h2) (CanReceiveBox h0 h3) (CanReceiveBox h0 h4) (CanReceiveBox h0 h5)
    (CanReceiveBox h1 h1) (CanReceiveBox h1 h2) (CanReceiveBox h1 h3) (CanReceiveBox h1 h4) (CanReceiveBox h1 h5)
    (CanReceiveBox h2 h2) (CanReceiveBox h2 h3) (CanReceiveBox h2 h4) (CanReceiveBox h2 h5)
    (CanReceiveBox h3 h3) (CanReceiveBox h3 h4) (CanReceiveBox h3 h5)
    (CanReceiveBox h4 h4) (CanReceiveBox h4 h5)
    (CanReceiveBox h5 h5)

    ;; Profilo iniziale delle colonne calpestabili (TopBox): Figura 1.1(a)
    (TopBox pos0  h2)
    (TopBox pos1  h2)
    (TopBox pos2  h2)   ; scala a terra qui
    (TopBox pos3  h3)
    (TopBox pos4  h2)
    (TopBox pos5  h3)
    (TopBox pos6  h2)
    (TopBox pos7  h1)
    (TopBox pos8  h2)   ; sotto la banana
    (TopBox pos9  h1)
    (TopBox pos10 h1)
    (TopBox pos11 h2)
    (TopBox pos12 h4)
    (TopBox pos13 h3)   ; Charlie qui
    (TopBox pos14 h4)
    (TopBox pos15 h3)
    (TopBox pos16 h4)
    (TopBox pos17 h5)
    (TopBox pos18 h4)
    (TopBox pos19 h3)
    (TopBox pos20 h4)   ; 4 scatole (allineato a specifica Fig. 1.1)

    ;; Charlie: in pos13 a quota h3 con mani libere
    (At pos13 h3)
    (HandsEmpty)

    ;; Scala: a terra in pos2 a quota h2
    (LadderAt pos2 h2)

    ;; Banana: appesa in pos8 a quota h4 (raggiungibile da Charlie a quota h3)
    (BananaAt pos8 h4)
  )

  (:goal
    (BananaGrabbed)
  )
)
