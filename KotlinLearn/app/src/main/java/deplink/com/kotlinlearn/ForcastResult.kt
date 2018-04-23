package deplink.com.kotlinlearn

/**
 * Created by ${kelijun} on 2018/4/23.
 */
data class ForcastResult(
        val city: City,
        val list: List<Forcast>)

data class City(val id:Long,val name:String,val coord:Coordinates,val country:String,val population:Int)
data class Coordinates(val lon:Float,val lat:Float)
data class Forcast(val dt:Long,val temp:Temperature,val pressure :Float,val humidity :Int,val weather:List<Weather>,
                   val speed:Float,val  deg:Int
                   )
data class Temperature(val dt:Long,val temp:Temperature,val pressure :Float)
