package net.ciphen.polyhoot.patterns.observer

interface Observer {
    fun update(o: Observable, arg: Any?)
}
