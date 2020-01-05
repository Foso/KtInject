package de.jensklingenberg.ktinject.di.data


import de.jensklingenberg.ktinject.annotations.Module
import de.jensklingenberg.ktinject.annotations.Provides
import de.jensklingenberg.ktinject.model.Motor

@Module
class SecondModule() {


    @Provides
    fun provideMotor()= Motor("test")


}

