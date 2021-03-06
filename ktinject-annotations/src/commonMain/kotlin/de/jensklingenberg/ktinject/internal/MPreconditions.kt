package de.jensklingenberg.ktinject.internal

class MPreconditions{
   companion object{

       /**
        * Ensures that an object reference passed as a parameter to the calling method is not null.
        *
        * @param reference an object reference
        * @param errorMessage the exception message to use if the check fails
        * @return the non-null reference that was validated
        * @throws NullPointerException if {@code reference} is null
        */
       fun <T> checkNotNull(reference: T?, errorMessage: String?=""): T {
           if (reference == null) {
               throw NullPointerException(errorMessage)
           }
           return reference
       }
   }
}