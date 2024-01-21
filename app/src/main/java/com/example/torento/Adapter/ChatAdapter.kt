import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.torento.DATACLASS.Message
import com.example.torento.R
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(currentUserId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val ITEM_RECEIVE = 1
    val ITEM_SENT = 2

    private val messages: MutableList<Message> = mutableListOf()

   // private var itemClickListener: OnItemClickListener? = null



    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType==1){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_receive_message, parent, false)
            return ReceiveViewHolder(view)
        }else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            return SendViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if(holder.javaClass == SendViewHolder::class.java){
            //sent
            val viewHolder = holder as SendViewHolder
            holder.sentMessage.text = message.text
        }else{
            //receive
            val viewHolder = holder as ReceiveViewHolder
            holder.receivedtext.text = message.text
        }


      //  holder.bind(message, currentUserId)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
         if(FirebaseAuth.getInstance().currentUser?.uid?.equals(message.senderId) == true){
             return ITEM_SENT
         }else{
             return ITEM_RECEIVE
         }

    }
    override fun getItemCount(): Int {
        return messages.size
    }

    class SendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


            val sentMessage: TextView = itemView.findViewById(R.id.messageTextView)


    }
    class ReceiveViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val receivedtext: TextView = itemView.findViewById(R.id.receive_txt)
    }
}