package com.example.torento.DATACLASS

data class Room(val sizeofroom:String,val roomdescriptiontext:String,
    val roomimageurl:String, val roomOwnerDpUrl : String)

data class Message(val senderId: String, val receiverId: String, val text: String, val timestamp: Long) {
    constructor() : this("","", "", 0)
}
data class Chat(
    val chatId: String = "", // Unique identifier for the chat
    val participants: Map<String, Boolean> = emptyMap(), // Map of participant user IDs
    val messages: Map<String, Message> = emptyMap() // Map of messages in the chat
)
data class MessageOwner(val senderId: String, val receiverId: String, val text: String, val name:String,val timestamp: Long) {
    constructor() : this("","", "","", 0)
}

data class Address(
    val states: List<String>,
    val districtsMap: Map<String, List<String>>
){
    companion object {
        fun getDefaultData(): Address {
            return Address(
                states = listOf("Select State", "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
                    "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
                    "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram",
                    "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
                    "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal"),
                districtsMap = mapOf(
                    "Select State" to listOf("Select District"),
                    "Andhra Pradesh" to listOf("Select District","Anantapur", "Chittoor", "East Godavari", "Guntur", "Krishna"),
                    "Arunachal Pradesh" to listOf("Select District","Anjaw", "Changlang", "East Kameng", "East Siang", "Kamle"),
                    "Assam" to listOf("Select District","Baksa", "Barpeta", "Biswanath", "Bongaigaon", "Cachar"),
                    "Bihar" to listOf("Araria", "Arwal", "Aurangabad", "Banka", "Begusarai"),
                    "Chhattisgarh" to listOf("Select District","Balod", "Baloda Bazar", "Balrampur", "Bastar", "Bemetara"),
                    "Goa" to listOf("Select District","North Goa", "South Goa"),
                    "Gujarat" to listOf("Select District","Ahmedabad", "Amreli", "Anand", "Aravalli", "Banaskantha"),
                    "Haryana" to listOf("Select District","Ambala", "Bhiwani", "Charkhi Dadri", "Faridabad", "Fatehabad"),
                    "Himachal Pradesh" to listOf("Select District","Bilaspur", "Chamba", "Hamirpur", "Kangra", "Kinnaur"),
                    "Jharkhand" to listOf("Select District","Bokaro", "Chatra", "Deoghar", "Dhanbad", "Dumka"),
                    "Karnataka" to listOf("Select District","Bagalkot", "Bangalore Rural", "Bangalore Urban", "Belgaum", "Bellary"),
                    "Kerala" to listOf("Select District","Alappuzha", "Ernakulam", "Idukki", "Kannur", "Kasaragod"),
                    "Madhya Pradesh" to listOf("Select District","Agar Malwa", "Alirajpur", "Anuppur", "Ashoknagar", "Balaghat"),
                    "Maharashtra" to listOf("Select District","Ahmednagar", "Akola", "Amravati", "Aurangabad", "Beed"),
                    "Manipur" to listOf("Select District","Bishnupur", "Chandel", "Churachandpur", "Imphal East", "Imphal West"),
                    "Meghalaya" to listOf("Select District","East Garo Hills", "East Jaintia Hills", "East Khasi Hills", "North Garo Hills", "Ri Bhoi"),
                    "Mizoram" to listOf("Select District","Aizawl", "Champhai", "Kolasib", "Lawngtlai", "Lunglei"),
                    "Nagaland" to listOf("Select District","Dimapur", "Kiphire", "Kohima", "Longleng", "Mokokchung"),
                    "Odisha" to listOf("Select District","Angul", "Balangir", "Balasore", "Bargarh", "Bhadrak"),
                    "Punjab" to listOf("Select District","Amritsar", "Barnala", "Bathinda", "Faridkot", "Fatehgarh Sahib"),
                    "Rajasthan" to listOf("Select District","Ajmer", "Alwar", "Banswara", "Baran", "Barmer"),
                    "Sikkim" to listOf("Select District","East Sikkim", "North Sikkim", "South Sikkim", "West Sikkim"),
                    "Tamil Nadu" to listOf("Select District","Ariyalur", "Chengalpattu", "Chennai", "Coimbatore", "Cuddalore"),
                    "Telangana" to listOf("Select District","Adilabad", "Bhadradri Kothagudem", "Hyderabad", "Jagtial", "Jangaon"),
                    "Tripura" to listOf("Select District","Dhalai", "Gomati", "Khowai", "North Tripura", "Sepahijala"),
                    "Uttar Pradesh" to listOf("Select District","Agra", "Aligarh", "Allahabad", "Ambedkar Nagar", "Amethi"),
                    "Uttarakhand" to listOf("Select District","Almora", "Bageshwar", "Chamoli", "Champawat", "Dehradun"),
                    "West Bengal" to listOf("Select District","Alipurduar", "Bankura", "Birbhum", "Cooch Behar", "Dakshin Dinajpur")
                )
            )
        }
    }
}

