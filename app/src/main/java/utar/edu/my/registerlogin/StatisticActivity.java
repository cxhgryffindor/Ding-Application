package utar.edu.my.registerlogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import org.checkerframework.checker.nullness.qual.NonNull;


public class StatisticActivity extends AppCompatActivity {

    private Button logout_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        //connect to firestore database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //get collection with collection path
        CollectionReference collectionRef = db.collection("task");

        // Create a query for documents with status
        Query query = collectionRef.whereEqualTo("status", 1);
        Query query2 = collectionRef.whereEqualTo("status", 0);

        //Bottom menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomViewNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_focus);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.bottom_home:
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.bottom_focus:
                    startActivity(new Intent(getApplicationContext(),focus_page.class));
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.bottom_profile:
                    return true;
            }
            return false;
        });

        //Button
        logout_button = findViewById(R.id.btn_logout);
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });


        collectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int totalDocuments = queryDocumentSnapshots.size();
                TextView textView = (TextView) findViewById(R.id.totaltask);
                textView.setText("" + totalDocuments);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {}
        });

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Get the number of documents with status=1
                    int count = task.getResult().size();
                    // Display the count on UI
                    TextView textView = (TextView) findViewById(R.id.completetask);
                    textView.setText("" + count);
                }
            }
        });

        query2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Get the number of documents with status=0
                    int count = task.getResult().size();
                    // Display the count on UI
                    TextView textView = (TextView) findViewById(R.id.ongoingtask);
                    textView.setText("" + count);
                }
            }
        });


        //connect to realtime database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Focus_time");

        // Attach a listener to the database reference
        ref.orderByKey().limitToLast(5).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                StringBuilder sb = new StringBuilder();
                long totalTime = 0;

                // Iterate over the children of the last 5 keys under Focus_time node
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get the activityName and time values
                    String activityName = snapshot.child("activityName").getValue(String.class);
                    long time = snapshot.child("time").getValue(Long.class);
                    totalTime += time;
                    // Append the activityName and time values to a string
                    sb.append(activityName).append(": ").append(time).append("s\n");
                }

                // Set the string as the text of the TextView
                TextView textView = (TextView) findViewById(R.id.countrecent);
                textView.setText(sb.toString());
                TextView textView2 = (TextView)findViewById(R.id.totaltime);
                textView2.setText("" + totalTime + "s");
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {}
        });
    }
}
