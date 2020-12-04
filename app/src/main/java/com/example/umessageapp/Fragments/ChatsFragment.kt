package com.example.umessageapp.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.umessageapp.AdapterClasses.UserAdapter
import com.example.umessageapp.Model.ChatList
import com.example.umessageapp.Model.User
import com.example.umessageapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatsFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<User>? = null
    private var usersChatList: List<ChatList>? = null
    lateinit var recycler_view_chatList: RecyclerView
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        recycler_view_chatList = view.findViewById(R.id.recycler_view_chatList)
        recycler_view_chatList.setHasFixedSize(true)
        recycler_view_chatList.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        usersChatList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
         ref!!.addValueEventListener(object : ValueEventListener{
             override fun onDataChange(p0: DataSnapshot)
             {
                 (usersChatList as ArrayList).clear()
                 for (dataSnapshot in p0.children)
                 {
                     val chatList = dataSnapshot.getValue(ChatList::class.java)
                     (usersChatList as ArrayList).add(chatList!!)
                 }
                 consultarChatList()
             }

             override fun onCancelled(p0: DatabaseError) {

             }
         })

        return view
    }

    private fun consultarChatList(){
        mUsers = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                (mUsers as ArrayList).clear()

                for (dataSnapshot in p0.children){
                    val user = dataSnapshot.getValue(User::class.java)

                    for (eachChatList  in usersChatList!!)
                    {
                        if (user!!.getUID().equals(eachChatList.getId()))
                        {
                            (mUsers as ArrayList).add(user!!)
                        }
                    }
                }
                userAdapter = UserAdapter(context!!, (mUsers as ArrayList<User>), true)
                recycler_view_chatList.adapter = userAdapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}