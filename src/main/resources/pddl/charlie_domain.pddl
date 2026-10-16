;;; DOMINIO CHARLIE - Pianificazione Classica STRIPS
;;; Esercitazione B.2.8 - Intelligenza Artificiale (Sapienza)

(define (domain charlie-strips)

  (:requirements :strips :typing)

  ;; TYPES
  (:types
    position  ; pos0 .. pos20  (coordinata orizzontale x)
    height    ; h0   .. h5     (quota/livello verticale h)
  )

  ;; PREDICATES
  (:predicates

    ;; TOPOLOGIA STATICA
    ;; (NextX ?x1 ?x2)        — ?x2 è la colonna immediatamente a destra di ?x1
    (NextX ?x1 - position ?x2 - position)

    ;; (NextH ?h1 ?h2)        — ?h2 è la quota immediatamente sopra ?h1 (h2 = h1 + 1)
    (NextH ?h1 - height ?h2 - height)

    ;; (SameH ?h1 ?h2)        — quote identiche (h1 == h2)
    (SameH ?ha - height ?hb - height)

    ;; (WalkableDiff ?h1 ?h2) — |h1 - h2| <= 1: Charlie può camminare senza scala
    (WalkableDiff ?ha - height ?hb - height)

    ;; (LadderDiff ?hlow ?hhigh) — hhigh == hlow + 2: superabile tramite scala
    (LadderDiff ?hlow - height ?hhigh - height)

    ;; (CanReceiveBox ?hDest ?h) — spazio libero per gravità alla quota di spinta (hDest <= h)
    (CanReceiveBox ?hDest - height ?h - height)

    ;; (Below ?h ?hBelow)     — inverso semantico di NextH (hBelow = h - 1, h > h0)
    (Below ?h - height ?hBelow - height)

    ;; FLUENTI DI COLONNA
    ;; (TopBox ?x ?h)  — la cima calpestabile della colonna ?x si trova a quota ?h
    (TopBox ?x - position ?h - height)

    ;; FLUENTI DI CHARLIE
    ;; (At ?x ?h)      — Charlie si trova in posizione ?x alla quota ?h
    (At ?x - position ?h - height)

    ;; (HasLadder)     — Charlie trasporta la scala
    (HasLadder)

    ;; (HandsEmpty)    — Charlie non trasporta la scala (mani libere)
    (HandsEmpty)

    ;; FLUENTI DELLA SCALA
    ;; (LadderAt ?x ?h) — la scala poggia a terra nella colonna ?x a quota ?h
    (LadderAt ?x - position ?h - height)

    ;; STATO BANANA
    ;; (BananaAt ?x ?h) — posizione statica della banana appesa
    (BananaAt ?x - position ?h - height)

    ;; (BananaGrabbed)  — banana raccolta con successo (obiettivo)
    (BananaGrabbed)
  )

  ;; AZIONI PRIMITIVE STRIPS

  ;; 1a. MOVE-RIGHT-EMPTY (mani libere, Δh <= 1)
  (:action move-right-empty
    :parameters (?x - position ?x2 - position ?h - height ?h2 - height)
    :precondition (and
      (At ?x ?h)
      (HandsEmpty)
      (NextX ?x ?x2)
      (TopBox ?x2 ?h2)
      (WalkableDiff ?h ?h2)
    )
    :effect (and
      (not (At ?x ?h))
      (At ?x2 ?h2)
    )
  )

  ;; 1b. MOVE-LEFT-EMPTY (mani libere, Δh <= 1)
  (:action move-left-empty
    :parameters (?x - position ?x2 - position ?h - height ?h2 - height)
    :precondition (and
      (At ?x ?h)
      (HandsEmpty)
      (NextX ?x2 ?x)
      (TopBox ?x2 ?h2)
      (WalkableDiff ?h ?h2)
    )
    :effect (and
      (not (At ?x ?h))
      (At ?x2 ?h2)
    )
  )

  ;; 2a. MOVE-RIGHT-CARRYING (trasporto scala, Δh <= 1)
  (:action move-right-carrying
    :parameters (?x - position ?x2 - position ?h - height ?h2 - height)
    :precondition (and
      (At ?x ?h)
      (HasLadder)
      (NextX ?x ?x2)
      (TopBox ?x2 ?h2)
      (WalkableDiff ?h ?h2)
    )
    :effect (and
      (not (At ?x ?h))
      (At ?x2 ?h2)
    )
  )

  ;; 2b. MOVE-LEFT-CARRYING (trasporto scala, Δh <= 1)
  (:action move-left-carrying
    :parameters (?x - position ?x2 - position ?h - height ?h2 - height)
    :precondition (and
      (At ?x ?h)
      (HasLadder)
      (NextX ?x2 ?x)
      (TopBox ?x2 ?h2)
      (WalkableDiff ?h ?h2)
    )
    :effect (and
      (not (At ?x ?h))
      (At ?x2 ?h2)
    )
  )

  ;; 3. HOLD-LADDER (raccolta scala a terra)
  (:action hold-ladder
    :parameters (?x - position ?h - height)
    :precondition (and
      (At ?x ?h)
      (HandsEmpty)
      (LadderAt ?x ?h)
    )
    :effect (and
      (not (HandsEmpty))
      (HasLadder)
      (not (LadderAt ?x ?h))
    )
  )

  ;; 4. DROP-LADDER (deposito scala a terra)
  (:action drop-ladder
    :parameters (?x - position ?h - height)
    :precondition (and
      (At ?x ?h)
      (HasLadder)
    )
    :effect (and
      (not (HasLadder))
      (HandsEmpty)
      (LadderAt ?x ?h)
    )
  )

  ;; 5a. CLIMB-LADDER-UP-RIGHT (salita Δh = 2 verso destra)
  ;;     Scala posizionata a quota inferiore nella colonna di partenza ?x
  (:action climb-ladder-up-right
    :parameters (?x - position ?x2 - position ?h - height ?h2 - height)
    :precondition (and
      (At ?x ?h)
      (HandsEmpty)
      (NextX ?x ?x2)
      (TopBox ?x2 ?h2)
      (LadderAt ?x ?h)
      (LadderDiff ?h ?h2)
    )
    :effect (and
      (not (At ?x ?h))
      (At ?x2 ?h2)
    )
  )

  ;; 5b. CLIMB-LADDER-UP-LEFT (salita Δh = 2 verso sinistra)
  (:action climb-ladder-up-left
    :parameters (?x - position ?x2 - position ?h - height ?h2 - height)
    :precondition (and
      (At ?x ?h)
      (HandsEmpty)
      (NextX ?x2 ?x)
      (TopBox ?x2 ?h2)
      (LadderAt ?x ?h)
      (LadderDiff ?h ?h2)
    )
    :effect (and
      (not (At ?x ?h))
      (At ?x2 ?h2)
    )
  )

  ;; 6a. CLIMB-LADDER-DOWN-RIGHT (discesa Δh = 2 verso destra)
  ;;     Scala posizionata a quota inferiore nella colonna di arrivo ?x2
  (:action climb-ladder-down-right
    :parameters (?x - position ?x2 - position ?h - height ?h2 - height)
    :precondition (and
      (At ?x ?h)
      (HandsEmpty)
      (NextX ?x ?x2)
      (TopBox ?x2 ?h2)
      (LadderAt ?x2 ?h2)
      (LadderDiff ?h2 ?h)
    )
    :effect (and
      (not (At ?x ?h))
      (At ?x2 ?h2)
    )
  )

  ;; 6b. CLIMB-LADDER-DOWN-LEFT (discesa Δh = 2 verso sinistra)
  (:action climb-ladder-down-left
    :parameters (?x - position ?x2 - position ?h - height ?h2 - height)
    :precondition (and
      (At ?x ?h)
      (HandsEmpty)
      (NextX ?x2 ?x)
      (TopBox ?x2 ?h2)
      (LadderAt ?x2 ?h2)
      (LadderDiff ?h2 ?h)
    )
    :effect (and
      (not (At ?x ?h))
      (At ?x2 ?h2)
    )
  )

  ;; 7a. PUSH-BOX-FLAT-RIGHT
  ;;     Charlie in ?x a ?h, scatola in ?x1 a ?hBox = ?h + 1.
  ;;     Colonna ?x1 scende a quota ?h; Charlie avanza in ?x1 a quota ?h.
  ;;     Colonna ?x2 a quota ?hDest riceve la scatola e sale a ?hDestTop.
  ;;     Vincolo non adiacenza: (CanReceiveBox ?hDest ?h)
  (:action push-box-flat-right
    :parameters (?x - position
                 ?x1 - position
                 ?x2 - position
                 ?h - height
                 ?hBox - height
                 ?hDest - height
                 ?hDestTop - height)
    :precondition (and
      (At ?x ?h)
      (HandsEmpty)
      (NextX ?x ?x1)
      (NextX ?x1 ?x2)
      (NextH ?h ?hBox)
      (TopBox ?x1 ?hBox)
      (TopBox ?x2 ?hDest)
      (CanReceiveBox ?hDest ?h)
      (NextH ?hDest ?hDestTop)
    )
    :effect (and
      (not (At ?x ?h))
      (At ?x1 ?h)
      (not (TopBox ?x1 ?hBox))
      (TopBox ?x1 ?h)
      (not (TopBox ?x2 ?hDest))
      (TopBox ?x2 ?hDestTop)
    )
  )

  ;; 7b. PUSH-BOX-FLAT-LEFT
  (:action push-box-flat-left
    :parameters (?x - position
                 ?x1 - position
                 ?x2 - position
                 ?h - height
                 ?hBox - height
                 ?hDest - height
                 ?hDestTop - height)
    :precondition (and
      (At ?x ?h)
      (HandsEmpty)
      (NextX ?x1 ?x)
      (NextX ?x2 ?x1)
      (NextH ?h ?hBox)
      (TopBox ?x1 ?hBox)
      (TopBox ?x2 ?hDest)
      (CanReceiveBox ?hDest ?h)
      (NextH ?hDest ?hDestTop)
    )
    :effect (and
      (not (At ?x ?h))
      (At ?x1 ?h)
      (not (TopBox ?x1 ?hBox))
      (TopBox ?x1 ?h)
      (not (TopBox ?x2 ?hDest))
      (TopBox ?x2 ?hDestTop)
    )
  )

  ;; 7c. PUSH-BOX-LADDER-RAMP-RIGHT (4 posizioni contigue, Fig. 1.1(f))
  ;;     Charlie in ?x a ?h, scatola in ?x1 a ?hBox = ?h + 1.
  ;;     Scala a terra in ?x2 a quota ?h (TopBox ?x2 ?h).
  ;;     La scatola sale la rampa, compie +2 e +1 e atterra su ?x3 a ?hDestTop.
  ;;     Charlie avanza di 2 posizioni atterrando in ?x2 dentro la scala a ?h.
  (:action push-box-ladder-ramp-right
    :parameters (?x - position
                 ?x1 - position
                 ?x2 - position
                 ?x3 - position
                 ?h - height
                 ?hBox - height
                 ?hDest - height
                 ?hDestTop - height)
    :precondition (and
      (At ?x ?h)
      (HandsEmpty)
      (NextX ?x ?x1)
      (NextX ?x1 ?x2)
      (NextX ?x2 ?x3)
      (NextH ?h ?hBox)
      (TopBox ?x1 ?hBox)
      (LadderAt ?x2 ?h)
      (TopBox ?x2 ?h)
      (TopBox ?x3 ?hDest)
      (NextH ?h ?hDest)
      (NextH ?hDest ?hDestTop)
    )
    :effect (and
      (not (At ?x ?h))
      (At ?x2 ?h)
      (not (TopBox ?x1 ?hBox))
      (TopBox ?x1 ?h)
      (not (TopBox ?x3 ?hDest))
      (TopBox ?x3 ?hDestTop)
    )
  )

  ;; 7d. PUSH-BOX-LADDER-RAMP-LEFT
  (:action push-box-ladder-ramp-left
    :parameters (?x - position
                 ?x1 - position
                 ?x2 - position
                 ?x3 - position
                 ?h - height
                 ?hBox - height
                 ?hDest - height
                 ?hDestTop - height)
    :precondition (and
      (At ?x ?h)
      (HandsEmpty)
      (NextX ?x1 ?x)
      (NextX ?x2 ?x1)
      (NextX ?x3 ?x2)
      (NextH ?h ?hBox)
      (TopBox ?x1 ?hBox)
      (LadderAt ?x2 ?h)
      (TopBox ?x2 ?h)
      (TopBox ?x3 ?hDest)
      (NextH ?h ?hDest)
      (NextH ?hDest ?hDestTop)
    )
    :effect (and
      (not (At ?x ?h))
      (At ?x2 ?h)
      (not (TopBox ?x1 ?hBox))
      (TopBox ?x1 ?h)
      (not (TopBox ?x3 ?hDest))
      (TopBox ?x3 ?hDestTop)
    )
  )

  ;; 7e. PUSH-FROM-LADDER-RIGHT (spinta da sopra la scala, Fig. 1.1(f))
  ;;     Charlie in ?x a quota ?h dentro la scala (LadderAt ?x ?h).
  ;;     Scatola in ?x1 a quota ?hBox = ?h + 2 (LadderDiff ?h ?hBox).
  ;;     Colonna ?x1 scende a quota gradino ?hStep = ?h + 1 (NextH ?h ?hStep).
  ;;     Charlie avanza in ?x1 posizionandosi alla nuova quota ?hStep.
  ;;     Colonna ?x2 a quota ?hDest riceve la scatola e sale a ?hDestTop.
  (:action push-from-ladder-right
    :parameters (?x - position
                 ?x1 - position
                 ?x2 - position
                 ?h - height
                 ?hBox - height
                 ?hStep - height
                 ?hDest - height
                 ?hDestTop - height)
    :precondition (and
      (At ?x ?h)
      (HandsEmpty)
      (LadderAt ?x ?h)
      (NextX ?x ?x1)
      (NextX ?x1 ?x2)
      (LadderDiff ?h ?hBox)
      (TopBox ?x1 ?hBox)
      (NextH ?h ?hStep)
      (TopBox ?x2 ?hDest)
      (CanReceiveBox ?hDest ?hStep)
      (NextH ?hDest ?hDestTop)
    )
    :effect (and
      (not (At ?x ?h))
      (At ?x1 ?hStep)
      (not (TopBox ?x1 ?hBox))
      (TopBox ?x1 ?hStep)
      (not (TopBox ?x2 ?hDest))
      (TopBox ?x2 ?hDestTop)
    )
  )

  ;; 7f. PUSH-FROM-LADDER-LEFT
  (:action push-from-ladder-left
    :parameters (?x - position
                 ?x1 - position
                 ?x2 - position
                 ?h - height
                 ?hBox - height
                 ?hStep - height
                 ?hDest - height
                 ?hDestTop - height)
    :precondition (and
      (At ?x ?h)
      (HandsEmpty)
      (LadderAt ?x ?h)
      (NextX ?x1 ?x)
      (NextX ?x2 ?x1)
      (LadderDiff ?h ?hBox)
      (TopBox ?x1 ?hBox)
      (NextH ?h ?hStep)
      (TopBox ?x2 ?hDest)
      (CanReceiveBox ?hDest ?hStep)
      (NextH ?hDest ?hDestTop)
    )
    :effect (and
      (not (At ?x ?h))
      (At ?x1 ?hStep)
      (not (TopBox ?x1 ?hBox))
      (TopBox ?x1 ?hStep)
      (not (TopBox ?x2 ?hDest))
      (TopBox ?x2 ?hDestTop)
    )
  )

  ;; 8. DIG (scavo della scatola calpestata)
  ;;    Elimina la scatola superiore; non consentito se la scala è nella stessa colonna a terra
  (:action dig
    :parameters (?x - position ?h - height ?hBelow - height)
    :precondition (and
      (At ?x ?h)
      (HandsEmpty)
      (TopBox ?x ?h)
      (Below ?h ?hBelow))
    :effect (and
      (not (TopBox ?x ?h))
      (TopBox ?x ?hBelow)
      (not (At ?x ?h))
      (At ?x ?hBelow)
    )
  )

  ;; 9. GRAB-BANANA (raccolta a quota hBanana - 1 a mani vuote)
  (:action grab-banana
    :parameters (?x - position ?hUnder - height ?hBanana - height)
    :precondition (and
      (At ?x ?hUnder)
      (HandsEmpty)
      (BananaAt ?x ?hBanana)
      (NextH ?hUnder ?hBanana)
    )
    :effect (and
      (BananaGrabbed)
    )
  )
)

