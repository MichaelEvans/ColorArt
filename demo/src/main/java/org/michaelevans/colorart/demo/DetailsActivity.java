/*
 * Copyright 2013 Michael Evans <michaelcevans10@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.michaelevans.colorart.demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.michaelevans.colorart.library.ColorArt;
import org.michaelevans.colorart.library.FadingImageView;

public class DetailsActivity extends Activity {

    private FadingImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        int position = 0;
        Bundle extras = getIntent().getExtras();
        if(extras != null)
            position = extras.getInt("position");

        Bitmap album = BitmapFactory.decodeResource(getResources(), SampleData.imageIds[position]);
        ColorArt colorArt = new ColorArt(album);

        mImageView = (FadingImageView) findViewById(R.id.image);
        mImageView.setImageBitmap(album);
        mImageView.setBackgroundColor(colorArt.getBackgroundColor(), FadingImageView.FadeSide.LEFT);

        View container = findViewById(R.id.container);
        container.setBackgroundColor(colorArt.getBackgroundColor());

        TextView primary = (TextView) findViewById(R.id.primary);
        primary.setTextColor(colorArt.getPrimaryColor());
        TextView secondary = (TextView) findViewById(R.id.secondary);
        secondary.setTextColor(colorArt.getSecondaryColor());
        TextView detail = (TextView) findViewById(R.id.detail);
        detail.setTextColor(colorArt.getDetailColor());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_fade) {
            if(item.isChecked()){
                mImageView.setFadeEnabled(true);
                item.setChecked(false);
            }else{
                mImageView.setFadeEnabled(false);
                item.setChecked(true);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
