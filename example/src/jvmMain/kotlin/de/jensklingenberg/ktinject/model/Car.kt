package de.jensklingenberg.ktinject.model

data class Car(val name:String,val motor: Motor, val wheel: Wheel){
    fun start(){
        println("Car started")
    }
}

data class Motor(val name : String)