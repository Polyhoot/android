package net.ciphen.polyhoot.game.event

enum class GameEventType {
    NOT_CONNECTED,
    CONNECTING,
    CONNECTED,
    START_GAME,
    GET_READY,
    QUESTION,
    TIME_UP,
    DEBUG_MESSAGE,
    FAIL,
    STATUS,
    NAME_TAKEN,
    FORCE_STOP,
    END
}
