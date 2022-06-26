package com.example.somp.fragments

import android.app.Application
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.example.somp.AudioClass
import com.example.somp.MainActivity
import com.example.somp.R
import kotlinx.android.synthetic.main.fragment_search.*
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val clas = AudioClass()
        val audioList = clas.mp3ReaderNew(application = context?.applicationContext as Application)
        editText.doAfterTextChanged { find(audioList, editText.text) }

    }
    private fun find(audioList: List<AudioClass.Audio>, text: Editable){
        try {
            search_layout.removeAllViews()
            audioList.forEach {
                if (it.name.contains(text)){
                    val ll = LinearLayout(context)
                    val img = ImageView(search_layout.context)
                    val tv = TextView(search_layout.context)
                    val drawable = resources.getDrawable(R.drawable.ic__music_note,search_layout.context!!.theme)
                    img.setImageDrawable(drawable)
                    tv.text = it.name.replace(".mp3", "") + "\n"
                    val name = it.name
                    ll.addView(img)
                    ll.addView(tv)
                    ll.setOnClickListener {layoutClick(name, audioList)}
                    search_layout.addView(ll)
                }
            }
        }
        catch (e: Exception){
            Toast.makeText(search_layout.context, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }
    private fun layoutClick(songName: String, audioList: List<AudioClass.Audio>){
        try {
            (activity as MainActivity?)?.setMusic(songName, audioList)
        }
        catch (e: Exception){
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}