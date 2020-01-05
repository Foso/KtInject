package de.jensklingenberg.ktinject.di


import de.jensklingenberg.ktinject.annotations.Module
import de.jensklingenberg.ktinject.annotations.Provides
import de.jensklingenberg.ktinject.model.Motor

@Module
class ThirdModule() {


    @Provides
    fun provideThirdMotor() = Motor("test")


}

