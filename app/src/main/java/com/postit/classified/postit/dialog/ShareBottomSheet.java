package com.postit.classified.postit.dialog;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.postit.classified.postit.R;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ShareBottomSheet extends BottomSheetDialogFragment
{
    private final String host = "https://www.postit.ng";
    // "/ad/2170/registered escaoe?source=andriod&via=copy_to_clipboard";
    private final String sharePath = "/%s/%d/%s?source=andriod&via=%s";

    private String title;
    private Intent intent;

    private PackageManager pm;
    private List<ResolveInfo> launchables;
    private View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    private void init() {
        pm = getContext().getPackageManager();
        launchables = pm.queryIntentActivities(intent, 0);
        Collections.sort(launchables, new ResolveInfo.DisplayNameComparator(pm));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_share, container, false);
        initViews();
        return view;
    }

    private void initViews() {
        RecyclerView apps = view.findViewById(R.id.dialog_share_apps);
        TextView title = view.findViewById(R.id.share_title);
        TextView cancel = view.findViewById(R.id.share_cancel);
        cancel.setOnClickListener( v -> {
            ShareBottomSheet.this.dismiss();
        });

        title.setText( this.title );
        AppsAdapter adapter = new AppsAdapter();
        apps.setAdapter( adapter );
        GridLayoutManager layout = new GridLayoutManager(getContext(), getSpanCount());
        apps.setLayoutManager(layout);

        apps.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 30;
                outRect.bottom = 30;
                outRect.left = 20;
                outRect.right = 20;

                if ( parent.getChildAdapterPosition( view ) == 0 )
                    outRect.top = 0;

                if ( parent.getChildAdapterPosition( view )
                        >= parent.getAdapter().getItemCount() - ( parent.getAdapter().getItemCount() % getSpanCount() ) )
                    outRect.bottom = 200;
            }
        });
    }

    private int getSpanCount() {
        return 3;
    }

    private class AppsAdapter extends RecyclerView.Adapter< AppHolder >
    {

        @NonNull
        @Override
        public AppHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.grid_item, parent, false);
            return new AppHolder( view );
        }

        @Override
        public void onBindViewHolder(@NonNull AppHolder holder, int position) {
            ResolveInfo resolveInfo = launchables.get(position);
            holder.name.setText( resolveInfo.loadLabel( pm ) );
            holder.image.setImageDrawable( resolveInfo.loadIcon( pm ) );

            holder.itemView.setOnClickListener( v -> {
                modifyIntent( position );
                startActivity( intent );
                dismiss();
            });
        }

        private void modifyIntent( int position )
        {
            ResolveInfo resolveInfo = launchables.get(position);
            intent.setComponent( new ComponentName( resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name ) );
            intent.setPackage( resolveInfo.activityInfo.packageName );
            String type = intent.getStringExtra( "type" );
            long id = intent.getLongExtra( "ad_id", -1 );
            String title = intent.getStringExtra( "title" ).replace( " ", "_" );
            String medium = resolveInfo.loadLabel( pm ).toString().replace( " ", "_" );

            String uri = host + String.format( Locale.ENGLISH, sharePath, type, id, title, medium );
            intent.putExtra(Intent.EXTRA_TEXT, uri );
        }

        @Override
        public int getItemCount() {
            return launchables.size();
        }
    }

    private class AppHolder extends RecyclerView.ViewHolder
    {
        private final ImageView image;
        private final TextView name;

        public AppHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_image);
            name = itemView.findViewById(R.id.item_name);
        }
    }
}
