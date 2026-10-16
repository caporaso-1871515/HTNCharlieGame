;; PROBLEMA PDDL CLASSICO: LIVELLO 1 (FACILE)
;; Ambiente: Griglia [pos0..pos8], quote [h0..h3]
;;
;; Profilo: [2, 2, 2, 3, 2, 2, 2, 1, 1]
;; Charlie in pos0 a h2 con mani libere (HandsEmpty).
;; Banana appesa in pos5 a h3 (raggiungibile ad altezza h2 = h3-1).
;; Dislivelli |Δh| <= 1: navigazione base e gradini standard.

(define (problem charlie-level1-easy)
  (:domain charlie-strips)

  (:objects
    pos0 pos1 pos2 pos3 pos4 pos5 pos6 pos7 pos8 - position
    h0 h1 h2 h3 - height
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

    ;; Relazioni verticali di adiacenza (NextH e Below)
    (NextH h0 h1)
    (NextH h1 h2)
    (NextH h2 h3)

    (Below h1 h0)
    (Below h2 h1)
    (Below h3 h2)

    ;; Stessa quota (SameH)
    (SameH h0 h0)
    (SameH h1 h1)
    (SameH h2 h2)
    (SameH h3 h3)

    ;; Dislivello calpestabile |Δh| <= 1 (WalkableDiff)
    (WalkableDiff h0 h0) (WalkableDiff h0 h1)
    (WalkableDiff h1 h0) (WalkableDiff h1 h1) (WalkableDiff h1 h2)
    (WalkableDiff h2 h1) (WalkableDiff h2 h2) (WalkableDiff h2 h3)
    (WalkableDiff h3 h2) (WalkableDiff h3 h3)

    ;; Dislivello da scala Δh = 2 (LadderDiff)
    (LadderDiff h0 h2) (LadderDiff h1 h3)

    ;; Spazio libero per gravità alla quota di spinta: hDest <= h (CanReceiveBox)
    (CanReceiveBox h0 h0) (CanReceiveBox h0 h1) (CanReceiveBox h0 h2) (CanReceiveBox h0 h3)
    (CanReceiveBox h1 h1) (CanReceiveBox h1 h2) (CanReceiveBox h1 h3)
    (CanReceiveBox h2 h2) (CanReceiveBox h2 h3)
    (CanReceiveBox h3 h3)

    ;; Profilo iniziale delle colonne calpestabili (TopBox)
    (TopBox pos0 h2)
    (TopBox pos1 h2)
    (TopBox pos2 h2)
    (TopBox pos3 h3)
    (TopBox pos4 h2)
    (TopBox pos5 h2)
    (TopBox pos6 h2)
    (TopBox pos7 h1)
    (TopBox pos8 h1)

    ;; Charlie: posizionato in pos0 a quota h2 con mani libere
    (At pos0 h2)
    (HandsEmpty)

    ;; Scala disponibile a terra in pos0 a quota h2
    (LadderAt pos0 h2)

    ;; Banana: appesa in pos5 a quota h3
    (BananaAt pos5 h3)
  )

  (:goal
    (BananaGrabbed)
  )
)
