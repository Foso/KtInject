        package de.jensklingenberg.ktinject

        import de.jensklingenberg.ktinject.internal.MPreconditions
        import de.jensklingenberg.ktinject.internal.Provider
        import de.jensklingenberg.ktinject.di.TestModule
import de.jensklingenberg.ktinject.di.TestModule_ProvideDatabaseNameFactory
import kotlin.String
import de.jensklingenberg.ktinject.di.TestModule_ProvideTestFactory
import de.jensklingenberg.ktinject.model.Test
import de.jensklingenberg.ktinject.di.TestModule_ProvideWheelFactory
import de.jensklingenberg.ktinject.model.Wheel
import de.jensklingenberg.ktinject.di.TestModule_ProvideCarFactory
import de.jensklingenberg.ktinject.model.Car
import de.jensklingenberg.ktinject.di.data.SecondModule
import de.jensklingenberg.ktinject.di.data.SecondModule_ProvideMotorFactory
import de.jensklingenberg.ktinject.model.Motor
import de.jensklingenberg.ktinject.di.AppComponent


        class KtInjectAppComponent(private val testModule : TestModule,private val secondModule : SecondModule) : AppComponent {
            
        private lateinit var provideDatabaseNameProvider: Provider<String>
private lateinit var provideTestProvider: Provider<Test>
private lateinit var provideWheelProvider: Provider<Wheel>
private lateinit var provideCarProvider: Provider<Car>
private lateinit var provideMotorProvider: Provider<Motor>

            
        init{
             initialize()
        }
        
        fun initialize() {
          this.provideDatabaseNameProvider = TestModule_ProvideDatabaseNameFactory.create(testModule)
            this.provideMotorProvider = SecondModule_ProvideMotorFactory.create(secondModule)

            this.provideTestProvider = TestModule_ProvideTestFactory.create(testModule)
this.provideWheelProvider = TestModule_ProvideWheelFactory.create(testModule)
this.provideCarProvider = TestModule_ProvideCarFactory.create(testModule,provideMotorProvider)
        }
      

        override fun inject(instance: MyPresenter) {injectMyPresenter(instance) }
override fun inject(instance: MySecondClass) {injectMySecondClass(instance) }

        private fun injectMyPresenter(instance: MyPresenter) {
 MyPresenter_MembersInjector.injectTest(instance,provideTestProvider.get())
 MyPresenter_MembersInjector.injectCar(instance,provideCarProvider.get())  }
private fun injectMySecondClass(instance: MySecondClass) {
 MySecondClass_MembersInjector.injectTest(instance,provideTestProvider.get())
 MySecondClass_MembersInjector.injectCar(instance,provideCarProvider.get())  }


        companion object {
            fun builder(): Builder =  Builder()
        }

        class Builder {
                         private  var testModule: TestModule? = null
 private  var secondModule: SecondModule? = null

             fun testModule(instance: TestModule): Builder {
       this.testModule = MPreconditions.checkNotNull(instance)
      return this
    }
 fun secondModule(instance: SecondModule): Builder {
       this.secondModule = MPreconditions.checkNotNull(instance)
      return this
    }

    
    fun build(): AppComponent{
                                            if (testModule == null) {
    testModule = TestModule()
   }
if (secondModule == null) {
    secondModule = SecondModule()
   }
                                 return KtInjectAppComponent(testModule!!,secondModule!!)
                                
    }
         }
        }