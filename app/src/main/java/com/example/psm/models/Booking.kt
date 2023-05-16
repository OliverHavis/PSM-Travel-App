import android.os.Parcel
import android.os.Parcelable
import com.example.psm.models.Destination

class Booking(
    val destination: Destination,
    val queryFrom: String,
    val queryTo: String,
    val queryDate: String,
    val queryNights: Int,
    val queryAdults: Int,
    val queryChildren: Int,
    val selectedExtras: List<String>,
    var totalPrice: Double
) : Parcelable {
    // Implement Parcelable methods
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(destination, flags)
        dest.writeString(queryFrom)
        dest.writeString(queryTo)
        dest.writeString(queryDate)
        dest.writeInt(queryNights)
        dest.writeInt(queryAdults)
        dest.writeInt(queryChildren)
        dest.writeStringList(selectedExtras)
        dest.writeDouble(totalPrice)
    }

    companion object CREATOR : Parcelable.Creator<Booking> {
        override fun createFromParcel(parcel: Parcel): Booking {
            val destination = parcel.readParcelable<Destination>(Destination::class.java.classLoader)
            val queryFrom = parcel.readString()
            val queryTo = parcel.readString()
            val queryDate = parcel.readString()
            val queryNights = parcel.readInt()
            val queryAdults = parcel.readInt()
            val queryChildren = parcel.readInt()
            val selectedExtras = mutableListOf<String>()
            parcel.readStringList(selectedExtras)
            val totalPrice = parcel.readDouble()

            return destination?.let {
                Booking(
                    destination = it,
                    queryFrom = queryFrom!!,
                    queryTo = queryTo!!,
                    queryDate = queryDate!!,
                    queryNights = queryNights,
                    queryAdults = queryAdults,
                    queryChildren = queryChildren,
                    selectedExtras = selectedExtras,
                    totalPrice = totalPrice
                )
            }!!
        }


        override fun newArray(size: Int): Array<Booking?> {
            return arrayOfNulls(size)
        }
    }
}
