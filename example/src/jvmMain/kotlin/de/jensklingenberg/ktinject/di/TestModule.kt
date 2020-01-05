package de.jensklingenberg.ktinject.di


import de.jensklingenberg.ktinject.annotations.Module
import de.jensklingenberg.ktinject.annotations.Provides
import de.jensklingenberg.ktinject.model.Car
import de.jensklingenberg.ktinject.model.Motor
import de.jensklingenberg.ktinject.model.Test
import de.jensklingenberg.ktinject.model.Wheel

@Module
class TestModule() {

    @Provides
    fun provideDatabaseName(): String {
        return "demo-dagger.db"
    }

    @Provides
    fun provideCar(motor: Motor) = Car("Honda", motor,Wheel())


    @Provides
    fun provideTest() = Test()

    @Provides
    fun provideWheel() = Wheel()


}
