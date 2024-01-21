import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.torento.DATACLASS.MessageOwner
import com.example.torento.R

class ChatListAdapter(currentUserId: String) : RecyclerView.Adapter<ChatListAdapter.MessageOwnerViewHolder>() {

    private val messages: MutableList<MessageOwner> = mutableListOf()
    val currentUserId = currentUserId
    private var itemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(receivedId:String)
    }
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }
    fun addMessage(message: MessageOwner) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListAdapter.MessageOwnerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return MessageOwnerViewHolder(view,itemClickListener,messages)
    }


    override fun onBindViewHolder(holder: ChatListAdapter.MessageOwnerViewHolder, position: Int) {
        val message = messages[position]

        holder.bind(message, currentUserId)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class MessageOwnerViewHolder(
        itemView: View,
        listner: OnItemClickListener?,
        messages: MutableList<MessageOwner>
    ) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener{
                val position = adapterPosition
                // Call the onItemClick method of the listener and pass the document ID
                if (listner != null) {
                    listner.onItemClick(messages[position].senderId)
                }
            }
        }
        fun bind(message: MessageOwner,currentUserId:String) {

            val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
            messageTextView.text = message.name
            if(message.senderId == currentUserId){
                messageTextView.gravity = Gravity.END
            } else{
                messageTextView.gravity = Gravity.START
            }

        }
    }
}