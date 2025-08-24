import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.chatroomapp.R
import com.example.chatroomapp.User

class UserListAdapter(context: Context, userList: List<User>) :
    ArrayAdapter<User>(context, 0, userList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.topic_layout, parent, false)
        }

        val currentUser = getItem(position)
        val userNameTextView: TextView = itemView!!.findViewById(R.id.topicTextView)

        userNameTextView.text = currentUser?.name

        return itemView
    }
}
