package simulator

/***
 * This class is a workaround until I have figured out how I can delete event handlers while iterating through them.
 */
class DeactiveableHandler<T>(val action: ((T,DeactiveableHandler<T>) -> Unit),
                             var enabled: Boolean = true) {

    fun doAction(obj: T){
        if (enabled){
            action(obj, this)
        }
    }
}