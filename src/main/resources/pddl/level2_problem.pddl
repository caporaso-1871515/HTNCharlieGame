;; PROBLEMA PDDL CLASSICO: LIVELLO 2 (MEDIO)
;; Ambiente: Griglia [pos0..pos11], quote [h0..h4]
;;
;; Profilo: [2, 2, 2, 4, 4, 2, 2, 2, 2, 2, 2, 2]
;; Charlie in pos0 a h2 con mani libere (HandsEmpty).
;; Scala a terra in pos1 a h2.
;; Banana in pos6 a h3.
;; Vincolo: dislivello tra pos2 (h2) e pos3 (h4) è Δh = 2.


(define (problem charlie-level2-medium)
  (:domain charlie-strips)

  (:objects
    pos0 pos1 pos2 pos3 pos4 pos5 pos6 pos7 pos8 pos9 pos10 pos11 - position
    h0 h1 h2 h3 h4 - height
  )

  (:init
    ;; Topologia orizzontale (NextX)
    (NextX pos0 pos1)
    (NextX pos1 pos2)
    (NextX pos2 pos3)
    (NextX pos3 pos4)
    (NextX pos4 pos5)
    (NextX pos5 pos6)
    (NextX pos6 pos7)
    (NextX pos7 pos8)
    (NextX pos8 pos9)
    (NextX pos9 pos10)
    (NextX pos10 pos11)

    ;; Relazioni verticali di adiacenza (NextH e Below)
    (NextH h0 h1)
    (NextH h1 h2)
    (NextH h2 h3)
    (NextH h3 h4)

    (Below h1 h0)
    (Below h2 h1)
    (Below h3 h2)
    (Below h4 h3)

    ;; Stessa quota (SameH)
    (SameH h0 h0)
    (SameH h1 h1)
    (SameH h2 h2)
    (SameH h3 h3)
    (SameH h4 h4)

    ;; Dislivello calpestabile |Δh| <= 1 (WalkableDiff)
    (WalkableDiff h0 h0) (WalkableDiff h0 h1)
    (WalkableDiff h1 h0) (WalkableDiff h1 h1) (WalkableDiff h1 h2)
    (WalkableDiff h2 h1) (WalkableDiff h2 h2) (WalkableDiff h2 h3)
    (WalkableDiff h3 h2) (WalkableDiff h3 h3) (WalkableDiff h3 h4)
    (WalkableDiff h4 h3) (WalkableDiff h4 h4)

    ;; Dislivello da scala Δh = 2 (LadderDiff)
    (LadderDiff h0 h2) (LadderDiff h1 h3) (LadderDiff h2 h4)

    ;; Spazio libero per gravità alla quota di spinta: hDest <= h (CanReceiveBox)
    (CanReceiveBox h0 h0) (CanReceiveBox h0 h1) (CanReceiveBox h0 h2) (CanReceiveBox h0 h3) (CanReceiveBox h0 h4)
    (CanReceiveBox h1 h1) (CanReceiveBox h1 h2) (CanReceiveBox h1 h3) (CanReceiveBox h1 h4)
    (CanReceiveBox h2 h2) (CanReceiveBox h2 h3) (CanReceiveBox h2 h4)
    (CanReceiveBox h3 h3) (CanReceiveBox h3 h4)
    (CanReceiveBox h4 h4)

    ;; Profilo iniziale delle colonne calpestabili (TopBox)
    (TopBox pos0 h2)
    (TopBox pos1 h2)
    (TopBox pos2 h2)
    (TopBox pos3 h4)
    (TopBox pos4 h3)
    (TopBox pos5 h2)
    (TopBox pos6 h2)
    (TopBox pos7 h2)
    (TopBox pos8 h2)
    (TopBox pos9 h2)
    (TopBox pos10 h2)
    (TopBox pos11 h2)

    ;; Charlie: in pos0 a quota h2 con mani libere
    (At pos0 h2)
    (HandsEmpty)

    ;; Scala: a terra in pos1 a quota h2
    (LadderAt pos1 h2)

    ;; Banana: appesa in pos6 a quota h3
    (BananaAt pos6 h3)
  )

  (:goal
    (BananaGrabbed)
  )
)
