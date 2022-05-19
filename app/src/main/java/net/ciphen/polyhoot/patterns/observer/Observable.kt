package net.ciphen.polyhoot.patterns.observer

interface Observable {
    val observers: MutableList<Observer>

    fun addObserver(o: Observer) {
        observers.add(o)
    }

    fun removeObserver(o: Observer) {
        observers.remove(o)
    }

    fun countObservers() {
        observers.size
    }

    fun notifyObservers() {
        observers.forEach {
            it.update(this, null)
        }
    }

    fun notifyObservers(arg: Any) {
        observers.forEach {
            it.update(this, arg)
        }
    }
}