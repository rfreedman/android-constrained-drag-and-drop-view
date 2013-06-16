package net.greybeardedgeek;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class HelloAndroidActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConstrainedDragAndDropView dndView = (ConstrainedDragAndDropView) findViewById(R.id.dndView);
        dndView.setDragHandle(findViewById(R.id.draggable));
        dndView.setAllowVerticalDrag(false);

        dndView.addDropTarget(findViewById(R.id.target1));
        dndView.addDropTarget(findViewById(R.id.target2));
        dndView.addDropTarget(findViewById(R.id.target3));

        dndView.setDropListener(new ConstrainedDragAndDropView.DropListener() {
            @Override
            public void onDrop(int dropIndex, View dropTarget) {
                Toast.makeText(HelloAndroidActivity.this, "Dropped on Target " + dropIndex, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(net.greybeardedgeek.R.menu.main, menu);
        return true;
    }

}

