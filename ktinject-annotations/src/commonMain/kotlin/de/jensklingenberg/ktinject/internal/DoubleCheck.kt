package de.jensklingenberg.ktinject.internal

class DoubleCheck<T>(provider: Provider<T>) : Provider<T>, Lazy<T> {


    override fun get(): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object{
        fun <T> provider(delegate : Provider<T>){

        }
    }

}