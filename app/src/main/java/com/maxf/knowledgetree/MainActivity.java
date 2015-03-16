package com.maxf.knowledgetree;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maxf.knowledgetree.com.maxf.knowledgetree.engine.Engine;
import com.maxf.knowledgetree.com.maxf.knowledgetree.engine.Info;
import com.maxf.knowledgetree.com.maxf.knowledgetree.engine.SwipeDismissListViewTouchListener;

import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;


public class MainActivity extends ActionBarActivity {
    private Engine E;
    private TextView.OnEditorActionListener addListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addKnowledge(v);//match this behavior to your 'Send' (or Confirm) button
            }
            return true;
        }
    };
    private TextView.OnFocusChangeListener editListener = new TextView.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                editKnowledge(v);
            }
        }
    };

    /*private SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(listView,
                        new SwipeDismissListViewTouchListener.OnDismissCallback() {
        *                     public void onDismiss(ListView listView, int[] reverseSortedPositions) {
            *                         for (int position : reverseSortedPositions) {
                *                             adapter.remove(adapter.getItem(position));
                *                         }
            *                         adapter.notifyDataSetChanged();
            *                     }
        *                 });
    * listView.setOnTouchListener(touchListener);
    * listView.setOnScrollListener(touchListener.makeScrollListener());*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        E = new Engine("tree.xml",this);
        TextView addView = (TextView) findViewById(R.id.edit_message);
        addView.setOnEditorActionListener(addListener);
        /*ViewGroup layout = (ViewGroup) findViewById(R.id.notes_layout);
        TextView tv = new TextView(this);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        try {
            tv.setText(E.writeXml(E.doc));
        }
        catch(TransformerConfigurationException e)
        {
            Log.e("log",e.toString());
        }
        catch(TransformerException e)
        {
            Log.e("log",e.toString());
        }
        layout.addView(tv);*/
        updateList();
        LinearLayout notes_layout = (LinearLayout) findViewById(R.id.notes_layout);
        for (int i = 0; i < notes_layout.getChildCount(); ++i){
            TextView note_view = (TextView) notes_layout.getChildAt(i).findViewById(R.id.content);
            note_view.setOnFocusChangeListener(editListener);
        }
    }

    public void updateList(){
        Info[] infos = null;
        try {
            infos = E.getInfos();
        }
        catch(XPathExpressionException e)
        {
            Log.e("Error",e.toString());
        }
        ViewGroup layout = (ViewGroup) findViewById(R.id.notes_layout);
        layout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < infos.length; i++) {
            Info info = infos[infos.length-i-1];
            View inflatedView = inflater.inflate(R.layout.note, null);

            LinearLayout note_layout = (LinearLayout) inflatedView.findViewById(R.id.note_layout);
            note_layout.setBackgroundColor(Color.parseColor("#" + info.getColor()));

            TextView path = (TextView) inflatedView.findViewById(R.id.path);
            path.setText(info.getPath());

            TextView title = (TextView) inflatedView.findViewById(R.id.title);
            if (info.hasTitle()) title.setText(info.getTitle());
            else title.setVisibility(View.GONE);

            TextView content = (TextView) inflatedView.findViewById(R.id.content);
            content.setText(info.content);

            TextView exp_content = (TextView) inflatedView.findViewById(R.id.expanded_content);
            if (info.hasMeta()) {
                String metaText = "";
                for (Map.Entry<String, String> entry : info.metaData.entrySet()) {
                    metaText += "\r\n" + entry.getKey() + ": " + entry.getValue();
                }
                exp_content.setText(metaText);
            }

            TextView expandButton = (TextView) inflatedView.findViewById(R.id.expand);
            if (!info.hasMeta()) expandButton.setVisibility(View.GONE);

            note_layout.setClickable(info.hasMeta());

            LinearLayout note_root = (LinearLayout) note_layout.getParent();
            note_root.setId(Integer.valueOf(info.id));

            layout.addView(inflatedView);
        }
    }

    public void onClickNote(View note_layout){
        TextView expandButton = (TextView) note_layout.findViewById(R.id.expand);
        if(expandButton.getVisibility() == View.VISIBLE) onExpandNote(note_layout);
        if(expandButton.getVisibility() == View.GONE) onContractNote(note_layout);
    }

    public void onExpandNote(View note_layout){
        TextView exp_content = (TextView) note_layout.findViewById(R.id.expanded_content);
        exp_content.setVisibility(View.VISIBLE);
        TextView exp_button = (TextView) note_layout.findViewById(R.id.expand);
        exp_button.setVisibility(View.GONE);
        TextView cont_button = (TextView) note_layout.findViewById(R.id.contract);
        cont_button.setVisibility(View.VISIBLE);
        note_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onContractNote(v);
            }
        });
    }

    public void onContractNote(View note_layout){
        TextView exp_content = (TextView) note_layout.findViewById(R.id.expanded_content);
        exp_content.setVisibility(View.GONE);
        TextView exp_button = (TextView) note_layout.findViewById(R.id.expand);
        exp_button.setVisibility(View.VISIBLE);
        TextView cont_button = (TextView) note_layout.findViewById(R.id.contract);
        cont_button.setVisibility(View.GONE);
        note_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExpandNote(v);
            }
        });
    }

    public void addKnowledge(View view){
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String knowledge = editText.getText().toString();
        try {
            E.addInfo(knowledge);
        }
        catch(Exception e)
        {
            Log.e("login activity",e.toString());
        }
        updateList();
        editText.setText("");
    }

    public void editKnowledge(View view){
        TextView note = (TextView) view;
        String knowledge = note.getText().toString();
        LinearLayout note_root = (LinearLayout) note.getParent().getParent();
        String id =  String.valueOf(note_root.getId());
        try {
            E.editInfo(id, knowledge);
        }
        catch(Exception e)
        {
            Log.e("login activity",e.toString());
        }
        updateList();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
