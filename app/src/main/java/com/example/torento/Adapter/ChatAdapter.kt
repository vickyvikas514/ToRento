import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.torento.DATACLASS.Message
import com.example.torento.R

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    private val messages: MutableList<Message> = mutableListOf()

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message) {
            val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
            messageTextView.text = message.text
        }
    }
}