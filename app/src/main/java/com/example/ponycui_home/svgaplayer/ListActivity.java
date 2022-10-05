package com.example.ponycui_home.svgaplayer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tantan.library.svga.ResourceKey;
import com.tantan.library.svga.SVGALoader;
import com.tantan.library.svga.SVGAnimationView;
import com.tantan.library.svga.SVGAnimationView.FrameMode;
import com.tantan.library.svga.data.cache.Resource;
import com.tantan.library.svga.data.request.RequestCallback;
import com.tantan.library.svga.exception.SVGAException;
import com.tantan.library.svga.utils.Log;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
  private RecyclerView mRecyclerView;
  private final List<String> items = new ArrayList<>();
  private HomeAdapter adapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list);
    mRecyclerView = findViewById(R.id.list_view);
    this.setupData();
    this.setupListView();
    Log.printMemoryInfo();
  }

  void setupData() {
    /**
     * core_super_like_dialog.svga.so=https://auto.tancdn.com/v1/raw/a13ffb79-a65f-45de-a9df-cd2e31d2d96209.so
     *
     * <p>core_quick_chat_dialog.svga.so=https://auto.tancdn.com/v1/raw/a8d557f0-4564-4769-b11e-ea1ca52d35b309.so
     *
     * <p>core_common_dialog_top_bg.svga.so=https://auto.tancdn.com/v1/raw/48b01864-e871-498a-9151-f761e3d3188708.so
     *
     * <p>core_boost_dialog.svga.so=https://auto.tancdn.com/v1/raw/62fdbffa-b7f5-4ab4-aab2-c100940a23f409.so
     *
     * <p>core_boost_dialog_opt.svga.so=https://auto.tancdn.com/v1/raw/52778ec5-0aef-47a5-a725-cdaffb856af208.so
     *
     * <p>core_business_conversation_boosting.svga.so=https://auto.tancdn.com/v1/raw/f0bd3648-1f28-4912-8450-81792533e95508.so
     *
     * <p>core_business_conversation_boosting_small.svga.so=https://auto.tancdn.com/v1/raw/a39aecc6-3012-4a02-97e2-6d9f9198d18008.so
     *
     * <p>core_boosting_homepage_top.svga.so=https://auto.tancdn.com/v1/raw/135e8823-4fef-4105-9f7d-42b2034c9c6f09.so
     *
     * <p>core_boosting_homepage_top_opt.svga.so=https://auto.tancdn.com/v1/raw/f4568317-28e5-48d5-9173-1a162743a13909.so
     */
    /*    this.items.add("https://auto.tancdn.com/v1/raw/62fdbffa-b7f5-4ab4-aab2-c100940a23f409.so");
    this.items.add("https://auto.tancdn.com/v1/raw/a13ffb79-a65f-45de-a9df-cd2e31d2d96209.so");
    this.items.add("https://auto.tancdn.com/v1/raw/a8d557f0-4564-4769-b11e-ea1ca52d35b309.so");
    this.items.add("https://auto.tancdn.com/v1/raw/48b01864-e871-498a-9151-f761e3d3188708.so");
    this.items.add("https://auto.tancdn.com/v1/raw/f0bd3648-1f28-4912-8450-81792533e95508.so");
    this.items.add("https://auto.tancdn.com/v1/raw/a39aecc6-3012-4a02-97e2-6d9f9198d18008.so");
    this.items.add("https://auto.tancdn.com/v1/raw/135e8823-4fef-4105-9f7d-42b2034c9c6f09.so");
    this.items.add("https://auto.tancdn.com/v1/raw/f4568317-28e5-48d5-9173-1a162743a13909.so");
    this.items.add("https://auto.tancdn.com/v1/raw/52778ec5-0aef-47a5-a725-cdaffb856af208.so");
    this.items.add("https://auto.tancdn.com/v1/raw/52778ec5-0aef-47a5-a725-cdaffb856af208.so");*/

    try {
      /*String[] paths = getAssets().list("svga");
      for (String path : paths) {
        items.add(path);
      }*/
      Field[] fields = SVGAResourceUtils.class.getDeclaredFields();
      for (Field field : fields) {
        items.add((String) field.get(null));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    Log.e("总共条数：" + items.size());
  }

  void setupListView() {
    // 设置布局管理器
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
    mRecyclerView.setLayoutManager(linearLayoutManager);
    // 设置 item 增加和删除时的动画
    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    adapter = new HomeAdapter(this, items);
    mRecyclerView.setAdapter(adapter);
  }

  public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {
    private List<String> mList;
    private Context mContext;

    public HomeAdapter(Context mContext, List<String> mList) {
      this.mContext = mContext;
      this.mList = mList;
    }

    public void removeData(int position) {
      mList.remove(position);
      notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View itemView =
          LayoutInflater.from(mContext).inflate(R.layout.activity_list_item, parent, false);
      return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
      holder.tvTitle.setText(mList.get(position));
      // Log.e("onBindViewHolder:" + holder.ivIcon.hashCode() + "->" + position);
      SVGALoader.with(ListActivity.this)
          .from(mList.get(position))
          .frameMode(FrameMode.AFTER)
          //
          // .audioEnable(true)
          .loadCallback(
              new RequestCallback() {
                @Override
                public void onResourceReady(
                    @NonNull ResourceKey key, @NonNull Resource<?> resource) {
                  // Log.printMemoryInfo();
                }

                @Override
                public void onLoadFailed(@NonNull ResourceKey key, @Nullable SVGAException e) {}
              })
          .into(holder.ivIcon);
    }

    @Override
    public int getItemCount() {
      return mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
      private TextView tvTitle;
      private SVGAnimationView ivIcon;

      public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        ivIcon = itemView.findViewById(R.id.image);
        tvTitle = itemView.findViewById(R.id.tv_title);
      }
    }
  }
}
