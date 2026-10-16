package it.uniroma1.charlie.core;

/**
 * Enumerazione delle azioni fisiche atomiche eseguibili nell'ambiente di
 * Charlie.
 * Condivise sia dal planner classico nello spazio degli stati
 * (ClassicalPlanner)
 * sia dal motore HTN gerarchico (HTNPlanner).
 */
public enum Action {

    MOVE_RIGHT("move-right", "!move_right") {
        @Override
        public boolean isApplicable(WorldState state) {
            int nx = state.getMonkeyX() + 1;
            if (nx >= state.getWidth())
                return false;
            int dh = state.getHeightAt(nx) - state.getMonkeyH();
            return Math.abs(dh) <= 1;
        }

        @Override
        public WorldState apply(WorldState state) {
            WorldState next = state.copy();
            int nx = next.getMonkeyX() + 1;
            next.setMonkeyX(nx);
            next.setMonkeyH(next.getHeightAt(nx));
            if (next.isHoldingLadder()) {
                next.setLadderX(nx);
                next.setLadderH(next.getMonkeyH());
            }
            return next;
        }
    },

    MOVE_LEFT("move-left", "!move_left") {
        @Override
        public boolean isApplicable(WorldState state) {
            int nx = state.getMonkeyX() - 1;
            if (nx < 0)
                return false;
            int dh = state.getHeightAt(nx) - state.getMonkeyH();
            return Math.abs(dh) <= 1;
        }

        @Override
        public WorldState apply(WorldState state) {
            WorldState next = state.copy();
            int nx = next.getMonkeyX() - 1;
            next.setMonkeyX(nx);
            next.setMonkeyH(next.getHeightAt(nx));
            if (next.isHoldingLadder()) {
                next.setLadderX(nx);
                next.setLadderH(next.getMonkeyH());
            }
            return next;
        }
    },

    HOLD_LADDER("hold-ladder", "!hold_ladder") {
        @Override
        public boolean isApplicable(WorldState state) {
            return !state.isHoldingLadder()
                    && state.getMonkeyX() == state.getLadderX()
                    && state.getMonkeyH() == state.getLadderH();
        }

        @Override
        public WorldState apply(WorldState state) {
            WorldState next = state.copy();
            next.setHoldingLadder(true);
            return next;
        }
    },

    DROP_LADDER("drop-ladder", "!drop_ladder") {
        @Override
        public boolean isApplicable(WorldState state) {
            return state.isHoldingLadder();
        }

        @Override
        public WorldState apply(WorldState state) {
            WorldState next = state.copy();
            next.setHoldingLadder(false);
            next.setLadderX(next.getMonkeyX());
            next.setLadderH(next.getMonkeyH());
            return next;
        }
    },

    CLIMB_LADDER_RIGHT("climb-ladder-right", "!climb_ladder_right") {
        @Override
        public boolean isApplicable(WorldState state) {
            if (state.isHoldingLadder())
                return false;
            int nx = state.getMonkeyX() + 1;
            if (nx >= state.getWidth())
                return false;
            int targetH = state.getHeightAt(nx);
            int dh = targetH - state.getMonkeyH();
            if (Math.abs(dh) != 2)
                return false;

            int lowerH = Math.min(state.getMonkeyH(), targetH);
            return (state.getLadderX() == state.getMonkeyX() || state.getLadderX() == nx)
                    && state.getLadderH() == lowerH;
        }

        @Override
        public WorldState apply(WorldState state) {
            WorldState next = state.copy();
            int nx = next.getMonkeyX() + 1;
            next.setMonkeyX(nx);
            next.setMonkeyH(next.getHeightAt(nx));
            return next;
        }
    },

    CLIMB_LADDER_LEFT("climb-ladder-left", "!climb_ladder_left") {
        @Override
        public boolean isApplicable(WorldState state) {
            if (state.isHoldingLadder())
                return false;
            int nx = state.getMonkeyX() - 1;
            if (nx < 0)
                return false;
            int targetH = state.getHeightAt(nx);
            int dh = targetH - state.getMonkeyH();
            if (Math.abs(dh) != 2)
                return false;

            int lowerH = Math.min(state.getMonkeyH(), targetH);
            return (state.getLadderX() == state.getMonkeyX() || state.getLadderX() == nx)
                    && state.getLadderH() == lowerH;
        }

        @Override
        public WorldState apply(WorldState state) {
            WorldState next = state.copy();
            int nx = next.getMonkeyX() - 1;
            next.setMonkeyX(nx);
            next.setMonkeyH(next.getHeightAt(nx));
            return next;
        }
    },

    PUSH_FLAT_RIGHT("push-flat-right", "!push_right") {
        @Override
        public boolean isApplicable(WorldState state) {
            if (state.isHoldingLadder())
                return false;
            int x = state.getMonkeyX();
            if (x + 2 >= state.getWidth())
                return false;
            int boxH = state.getHeightAt(x + 1);
            if (boxH != state.getMonkeyH() + 1 || boxH <= 0)
                return false;
            return state.getHeightAt(x + 2) < boxH;
        }

        @Override
        public WorldState apply(WorldState state) {
            WorldState next = state.copy();
            int x = next.getMonkeyX();
            next.decrementHeightAt(x + 1);
            next.incrementHeightAt(x + 2);
            next.setMonkeyX(x + 1);
            next.setMonkeyH(next.getHeightAt(x + 1));
            return next;
        }
    },

    PUSH_FLAT_LEFT("push-flat-left", "!push_left") {
        @Override
        public boolean isApplicable(WorldState state) {
            if (state.isHoldingLadder())
                return false;
            int x = state.getMonkeyX();
            if (x - 2 < 0)
                return false;
            int boxH = state.getHeightAt(x - 1);
            if (boxH != state.getMonkeyH() + 1 || boxH <= 0)
                return false;
            return state.getHeightAt(x - 2) < boxH;
        }

        @Override
        public WorldState apply(WorldState state) {
            WorldState next = state.copy();
            int x = next.getMonkeyX();
            next.decrementHeightAt(x - 1);
            next.incrementHeightAt(x - 2);
            next.setMonkeyX(x - 1);
            next.setMonkeyH(next.getHeightAt(x - 1));
            return next;
        }
    },

    PUSH_LADDER_RAMP_RIGHT("push-ladder-ramp-right", "!push_ladder_ramp_right") {
        @Override
        public boolean isApplicable(WorldState state) {
            if (state.isHoldingLadder())
                return false;
            int x = state.getMonkeyX();
            if (x + 3 >= state.getWidth())
                return false;
            if (state.getHeightAt(x + 1) != state.getMonkeyH() + 1)
                return false;
            if (state.getLadderX() != x + 2 || state.getLadderH() != state.getMonkeyH())
                return false;
            if (state.getHeightAt(x + 2) != state.getMonkeyH())
                return false;
            return state.getHeightAt(x + 3) == state.getMonkeyH() + 1;
        }

        @Override
        public WorldState apply(WorldState state) {
            WorldState next = state.copy();
            int x = next.getMonkeyX();
            next.decrementHeightAt(x + 1);
            next.incrementHeightAt(x + 3);
            next.setMonkeyX(x + 2);
            next.setMonkeyH(state.getMonkeyH());
            return next;
        }
    },

    PUSH_LADDER_RAMP_LEFT("push-ladder-ramp-left", "!push_ladder_ramp_left") {
        @Override
        public boolean isApplicable(WorldState state) {
            if (state.isHoldingLadder())
                return false;
            int x = state.getMonkeyX();
            if (x - 3 < 0)
                return false;
            if (state.getHeightAt(x - 1) != state.getMonkeyH() + 1)
                return false;
            if (state.getLadderX() != x - 2 || state.getLadderH() != state.getMonkeyH())
                return false;
            if (state.getHeightAt(x - 2) != state.getMonkeyH())
                return false;
            return state.getHeightAt(x - 3) == state.getMonkeyH() + 1;
        }

        @Override
        public WorldState apply(WorldState state) {
            WorldState next = state.copy();
            int x = next.getMonkeyX();
            next.decrementHeightAt(x - 1);
            next.incrementHeightAt(x - 3);
            next.setMonkeyX(x - 2);
            next.setMonkeyH(state.getMonkeyH());
            return next;
        }
    },

    PUSH_FROM_LADDER_RIGHT("push-from-ladder-right", "!push_from_ladder_right") {
        @Override
        public boolean isApplicable(WorldState state) {
            if (state.isHoldingLadder())
                return false;
            int x = state.getMonkeyX();
            if (state.getLadderX() != x || state.getLadderH() != state.getMonkeyH())
                return false;
            if (x + 2 >= state.getWidth())
                return false;
            int boxH = state.getHeightAt(x + 1);
            if (boxH != state.getMonkeyH() + 1 && boxH != state.getMonkeyH() + 2)
                return false;
            return state.getHeightAt(x + 2) <= boxH - 1;
        }

        @Override
        public WorldState apply(WorldState state) {
            WorldState next = state.copy();
            int x = next.getMonkeyX();
            next.decrementHeightAt(x + 1);
            next.incrementHeightAt(x + 2);
            next.setMonkeyX(x + 1);
            next.setMonkeyH(next.getHeightAt(x + 1));
            return next;
        }
    },

    PUSH_FROM_LADDER_LEFT("push-from-ladder-left", "!push_from_ladder_left") {
        @Override
        public boolean isApplicable(WorldState state) {
            if (state.isHoldingLadder())
                return false;
            int x = state.getMonkeyX();
            if (state.getLadderX() != x || state.getLadderH() != state.getMonkeyH())
                return false;
            if (x - 2 < 0)
                return false;
            int boxH = state.getHeightAt(x - 1);
            if (boxH != state.getMonkeyH() + 1 && boxH != state.getMonkeyH() + 2)
                return false;
            return state.getHeightAt(x - 2) <= boxH - 1;
        }

        @Override
        public WorldState apply(WorldState state) {
            WorldState next = state.copy();
            int x = next.getMonkeyX();
            next.decrementHeightAt(x - 1);
            next.incrementHeightAt(x - 2);
            next.setMonkeyX(x - 1);
            next.setMonkeyH(next.getHeightAt(x - 1));
            return next;
        }
    },

    DIG("dig", "!dig") {
        @Override
        public boolean isApplicable(WorldState state) {
            if (state.isHoldingLadder())
                return false;
            int x = state.getMonkeyX();
            // Non consentito se la scala è a terra nella stessa colonna
            if (!state.isHoldingLadder() && state.getLadderX() == x)
                return false;
            // Non consentito scavare sul pavimento indistruttibile (quota <= 1)
            return state.getHeightAt(x) > 1 && state.getMonkeyH() == state.getHeightAt(x);
        }

        @Override
        public WorldState apply(WorldState state) {
            WorldState next = state.copy();
            int x = next.getMonkeyX();
            next.decrementHeightAt(x);
            next.setMonkeyH(next.getMonkeyH() - 1);
            return next;
        }
    },

    GRAB_BANANA("grab-banana", "!grab_banana") {
        @Override
        public boolean isApplicable(WorldState state) {
            return !state.hasBanana() && state.isDirectlyUnderBanana();
        }

        @Override
        public WorldState apply(WorldState state) {
            WorldState next = state.copy();
            next.setHasBanana(true);
            return next;
        }
    };

    private final String pddlName;
    private final String htnOperatorName;

    Action(String pddlName, String htnOperatorName) {
        this.pddlName = pddlName;
        this.htnOperatorName = htnOperatorName;
    }

    public String getPddlName() {
        return pddlName;
    }

    public String getHtnOperatorName() {
        return htnOperatorName;
    }

    public abstract boolean isApplicable(WorldState state);

    public abstract WorldState apply(WorldState state);

    public static Action fromHtnName(String htnName) {
        for (Action a : values()) {
            if (a.htnOperatorName.equalsIgnoreCase(htnName)
                    || a.htnOperatorName.replace("!", "").equalsIgnoreCase(htnName)) {
                return a;
            }
        }
        return null;
    }

    public static Action fromPddlName(String pddlName) {
        for (Action a : values()) {
            if (a.pddlName.equalsIgnoreCase(pddlName)) {
                return a;
            }
        }
        return null;
    }
}
