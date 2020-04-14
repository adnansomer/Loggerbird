package deneme.example.filedeneme


import android.widget.ImageView
import java.io.Serializable


data class RecyclerModel(var name:String, var imageUrl:String) : Serializable {

    companion object{

        fun getCountryList(): List<RecyclerModel> {
            val countryList = ArrayList<RecyclerModel>()
            countryList.clear()
            countryList.add(RecyclerModel("Turkiye", "https://raw.githubusercontent.com/AndroidCodility/Picasso-RecyclerView/master/images/cup_cake.png"))
            countryList.add(RecyclerModel("Turkiye", "https://raw.githubusercontent.com/AndroidCodility/Picasso-RecyclerView/master/images/cup_cake.png"))
            countryList.add(RecyclerModel("Turkiye", "https://raw.githubusercontent.com/AndroidCodility/Picasso-RecyclerView/master/images/cup_cake.png"))
            countryList.add(RecyclerModel("Turkiye", "https://raw.githubusercontent.com/AndroidCodility/Picasso-RecyclerView/master/images/cup_cake.png"))
            countryList.add(RecyclerModel("Turkiye", "https://raw.githubusercontent.com/AndroidCodility/Picasso-RecyclerView/master/images/cup_cake.png"))
            countryList.add(RecyclerModel("Turkiye", "https://raw.githubusercontent.com/AndroidCodility/Picasso-RecyclerView/master/images/cup_cake.png"))
            countryList.add(RecyclerModel("Turkiye", "https://raw.githubusercontent.com/AndroidCodility/Picasso-RecyclerView/master/images/cup_cake.png"))
            countryList.add(RecyclerModel("Turkiye", "https://raw.githubusercontent.com/AndroidCodility/Picasso-RecyclerView/master/images/cup_cake.png"))
            countryList.add(RecyclerModel("Turkiye", "https://raw.githubusercontent.com/AndroidCodility/Picasso-RecyclerView/master/images/cup_cake.png"))
            countryList.add(RecyclerModel("Turkiye", "https://raw.githubusercontent.com/AndroidCodility/Picasso-RecyclerView/master/images/cup_cake.png"))
            countryList.add(RecyclerModel("Turkiye", "https://raw.githubusercontent.com/AndroidCodility/Picasso-RecyclerView/master/images/cup_cake.png"))
            countryList.add(RecyclerModel("Turkiye", "https://raw.githubusercontent.com/AndroidCodility/Picasso-RecyclerView/master/images/cup_cake.png"))
            countryList.add(RecyclerModel("Turkiye", "https://raw.githubusercontent.com/AndroidCodility/Picasso-RecyclerView/master/images/cup_cake.png"))
            countryList.add(RecyclerModel("Turkiye", "https://raw.githubusercontent.com/AndroidCodility/Picasso-RecyclerView/master/images/cup_cake.png"))

            return countryList
        }



    }
}
