package com.hmy.apbs.apbs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.RefreshableListView;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.ags.geoprocessing.GPFeatureRecordSetLayer;
import com.esri.core.tasks.ags.geoprocessing.GPParameter;
import com.esri.core.tasks.ags.geoprocessing.GPResultResource;
import com.esri.core.tasks.ags.geoprocessing.Geoprocessor;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity implements LocationListener {
    MapView mMapView = null;
    GraphicsLayer graphicsLayer;
    String queryLayer;
    ProgressDialog progress;
    private ArrayList<GPParameter> params;
    Geoprocessor gp;
    private RefreshableListView mListView;
    private RefreshableListView hListView;
//    CallJavaWeb CallJavaWeb;
    String mTaskID;
    private TextView LocationResult;
    public LocationClient mLocationClient;
    ArrayList<HashMap<String, Object>> data;
    Dialog dialog;
    private TabHost tabhost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        graphicsLayer = new GraphicsLayer();
        mMapView = (MapView) findViewById(R.id.map);
        ArcGISTiledMapServiceLayer MyTiledMapServiceLayer = new ArcGISTiledMapServiceLayer(
                "http://cache1.arcgisonline.cn/ArcGIS/rest/services/ChinaOnlineCommunity/MapServer");
        mMapView.enableWrapAround(true);
        mMapView.centerAndZoom(30.251017, 120.171837, 14);
        mMapView.addLayer(MyTiledMapServiceLayer);
        mListView = (RefreshableListView)findViewById(R.id.nlistview);
        mListView.setOnRefreshListener(new RefreshableListView.OnRefreshListener() {
            @Override
            public void onRefresh(RefreshableListView listView) {
                new NewDataTask().execute();
            }
        });
        // 获取TabHost对象
        tabhost = (TabHost) findViewById(R.id.mytab);
        // 如果没有继承TabActivity时，通过该种方法加载启动tabHost
        tabhost.setup();
        tabhost.addTab(tabhost.newTabSpec("one").setIndicator("当前任务").setContent(R.id.firstTab));

        tabhost.addTab(tabhost.newTabSpec("two").setIndicator("历史任务").setContent(R.id.secondTab));

        hListView = (RefreshableListView)findViewById(R.id.ylistview);
        hListView.setOnRefreshListener(new RefreshableListView.OnRefreshListener() {
            @Override
            public void onRefresh(RefreshableListView listView) {
                new NewDataTask().execute();
            }
        });
        //CallJavaWeb
        mTaskID = "30594";
        CallJavaWeb CallJavaWeb = new CallJavaWeb(handler);
        CallJavaWeb.doStart(mTaskID);
        //定位
        LocationResult = (TextView)findViewById(R.id.carInfos);
        ((MainApplication)getApplication()).mLocationResult = LocationResult;
        mLocationClient = ((MainApplication)getApplication()).mLocationClient;
        initLocation();
        mLocationClient.start();
    }
    //开启线程
    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
                switch (msg.what)
                {
                    case 0:
                        // 拿出msg中的数据并显示出来
                        Bundle bundle = msg.getData();
                  String jsonstr = bundle.getString("result");
                        try{
                        JSONArray strsarray = new JSONArray(jsonstr);
                        intoList(strsarray);
                    }catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                        break;
                    default:
                        break;
                }
        };
    };
    //数据格式转换      2
    private void intoList(JSONArray strsarray){
        try {
             data = new ArrayList<HashMap<String, Object>>();
            for (int i = 0; i < strsarray.length(); i++) {
                HashMap<String, Object> mymap = new HashMap<String, Object>();
                mymap.put("carTaskID", strsarray.getJSONObject(i).get("carTaskID").toString());
                mymap.put("turninNum", strsarray.getJSONObject(i).get("turninNum").toString());
                mymap.put("turnoutNum", strsarray.getJSONObject(i).get("turnoutNum").toString());
                mymap.put("startPointNO", strsarray.getJSONObject(i).get("startPointNO").toString());
                mymap.put("endPointNO", strsarray.getJSONObject(i).get("endPointNO").toString());
                data.add(mymap);
            }
            System.out.println(data);
            LeagueAdapter adapter=new LeagueAdapter(data);
            mListView.setAdapter(adapter);
            dialogAdapter dialogAdapter=new dialogAdapter(data);
            hListView.setAdapter(dialogAdapter);

//            progressDialog.dismiss();
        }catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //填充ListView   3
    class LeagueAdapter extends BaseAdapter {

        private ArrayList<HashMap<String, Object>> data;
        private MyView views;
        public LeagueAdapter(ArrayList<HashMap<String, Object>> data){

            this.data=data;
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView = MainActivity.this.getLayoutInflater().inflate(R.layout.tasklist_item,null);
                views=new MyView();
                views.TaskID=(TextView)convertView.findViewById(R.id.TaskID);
                views.startPoint=(TextView)convertView.findViewById(R.id.startPoint);
                views.stopPoint=(TextView)convertView.findViewById(R.id.stopPoint);
                convertView.setTag(views);
            }else{
                views=(MyView)convertView.getTag();
            }
            final String TaskID = data.get(position).get("carTaskID").toString();
            final String startPoint = data.get(position).get("startPointNO").toString();
            final String stopPoint = data.get(position).get("endPointNO").toString();
            views.TaskID.setText(TaskID);
            views.startPoint.setText(startPoint);
            views.stopPoint.setText(stopPoint);
            //设置点击进入详情的点击事件
            views.TaskID.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplication(),"点击了任务",Toast.LENGTH_SHORT).show();
                    startQuery(startPoint,stopPoint);
                }

            });
            views.startPoint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplication(),"点击了任务",Toast.LENGTH_SHORT).show();
                    startQuery(startPoint, stopPoint);
                }

            });
            views.stopPoint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplication(),"点击了任务",Toast.LENGTH_SHORT).show();
                    startQuery(startPoint,stopPoint);
                }

            });
            return convertView;
        }
        class MyView{
            public TextView TaskID;
            public TextView startPoint;
            public TextView stopPoint;
        }
    }
    //开始查询**--**-
    private void startQuery(final String firstNO, final String endNO){
                //声明查询
                queryLayer = "http://219.231.176.27:6080/arcgis/rest/services/PublicBicyclePoint/FeatureServer";
                String targetLayer = queryLayer.concat("/1");
                String[] queryArray = {targetLayer, "NO = " +firstNO+ " OR NO = " +endNO+ ""};
                AsyncQueryTask ayncQuery = new AsyncQueryTask();
                ayncQuery.execute(queryArray);
    }
    //查询任务
    private class AsyncQueryTask extends AsyncTask<String, Void, FeatureResult> {

                @Override
                protected void onPreExecute () {
                progress = new ProgressDialog(MainActivity.this);
                progress = ProgressDialog.show(MainActivity.this, "",
                        "正在查询...");
            }
                @Override
                protected FeatureResult doInBackground (String...queryArray){
                if (queryArray == null || queryArray.length <= 1)
                    return null;
                String url = queryArray[0];
                QueryParameters qParameters = new QueryParameters();
                String whereClause = queryArray[1];
                ;
                SpatialReference sr = SpatialReference.create(102100);
                qParameters.setOutSpatialReference(sr);
                qParameters.setReturnGeometry(true);
                qParameters.setWhere(whereClause);
                QueryTask qTask = new QueryTask(url);
                try {
                    FeatureResult results = qTask.execute(qParameters);
                    return results;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
                @Override
                protected void onPostExecute (FeatureResult results){
                Feature[] features = new Feature[2];
                if (results != null) {
                    int size = (int) results.featureCount();
                    int i = 0;
                    for (Object element : results) {
                        Feature feature = (Feature) element;
                        features[i] = feature;
                        i++;
                        Graphic graphic = new Graphic(feature.getGeometry(),
                                new SimpleMarkerSymbol(
                                        Color.RED, 10, SimpleMarkerSymbol.STYLE.CIRCLE));
                        graphicsLayer.addGraphic(graphic);
                        mMapView.addLayer(graphicsLayer);
                        mMapView.setExtent(graphic.getGeometry());
                    }
                    progress.dismiss();
                    //开始运行GP工具
                    startGP(features);
                } else {
                    Toast.makeText(getApplicationContext(), "Resule等于空", Toast.LENGTH_SHORT).show();
                }
                progress.dismiss();
            }
    }
    //生成任务路径
    public void startGP(Feature[] features) {
        Feature startfeature = features[0];
        Feature stopfeature = features[1];
        GPFeatureRecordSetLayer gpf = new GPFeatureRecordSetLayer(
                "inputPoint");
        gpf.setSpatialReference(mMapView.getSpatialReference());
        gpf.setGeometryType(Geometry.Type.POINT);
        Graphic startPoint = new Graphic(startfeature.getGeometry(), new SimpleMarkerSymbol(Color.RED, 25,
                SimpleMarkerSymbol.STYLE.DIAMOND));
        Graphic stopPoint = new Graphic(stopfeature.getGeometry(), new SimpleMarkerSymbol(Color.BLUE, 25,
                SimpleMarkerSymbol.STYLE.DIAMOND));
        gpf.addGraphic(startPoint);
        gpf.addGraphic(stopPoint);
        params = new ArrayList<GPParameter>();
        params.add(gpf);
        new TaskLineQuery().execute(params);
    }
    //添加GP工具返回的结果到图层
    class TaskLineQuery extends
            AsyncTask<ArrayList<GPParameter>, Void, GPParameter[]> {
        GPParameter[] outParams = null;
        ProgressDialog fprogress = null;
        protected void onPreExecute () {
            fprogress = new ProgressDialog(MainActivity.this);
            fprogress = ProgressDialog.show(MainActivity.this, "",
                    "正在生成任务路线...");
        }
        protected GPParameter[] doInBackground(ArrayList<GPParameter>... params1) {
            gp = new Geoprocessor(
                    "http://219.231.176.27:6080/arcgis/rest/services/GP/taskLinesapp/GPServer/taskLinesapp");
            SpatialReference sr = SpatialReference.create(102100);
            gp.setOutSR(sr);
            try {
                GPResultResource rr = gp.execute(params1[0]);
                outParams = rr.getOutputParameters();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return outParams;
        }
        @Override
        protected void onPostExecute(GPParameter[] result) {
            if (result == null)
                return;
            for (int i = 0; i < result.length; i++) {
                if (result[i] instanceof GPFeatureRecordSetLayer) {
                    GPFeatureRecordSetLayer fsl = (GPFeatureRecordSetLayer) result[i];
                    for (Graphic feature : fsl.getGraphics()) {
                        Graphic g = new Graphic(feature.getGeometry(),
                                new SimpleLineSymbol(Color.RED,2));
                        graphicsLayer.addGraphic(g);
                        mMapView.addLayer(graphicsLayer);
                        mMapView.setExtent(g.getGeometry());
                    }
                }
            }
            fprogress.dismiss();
        }
    }
    //下拉刷新方法
    private class NewDataTask extends AsyncTask<Void, Void, String> {

        ProgressDialog sprogress = null;
        protected void onPreExecute () {
            sprogress = new ProgressDialog(MainActivity.this);
            sprogress = ProgressDialog.show(MainActivity.this, "",
                    "正在刷新...");
        }
        @Override
        protected String doInBackground(Void... params) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {}

            return "A new list item";
        }
        @Override
        protected void onPostExecute(String result) {
            CallJavaWeb CallJavaWeb = new CallJavaWeb(handler);
            CallJavaWeb.doStart("30594");
            mListView.completeRefreshing();
            sprogress.dismiss();
            super.onPostExecute(result);
        }
    }

    private void initLocation(){

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("gcj02");
        option.setScanSpan(2000);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(true);
        mLocationClient.setLocOption(option);

    }
//    //历史任务记录dialog
//    public void historyTasks(){
//        View view1 = getLayoutInflater().inflate(R.layout.historytasks,null);
//        dialog = new Dialog(MainActivity.this,R.style.transparentFrameWindowStyle);
//        dialog.setContentView(view1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//       hListView = (RefreshableListView)view1.findViewById(R.id.ylistview);
//       dialogAdapter dialogAdapter = new dialogAdapter(data);
//       hListView.setAdapter(dialogAdapter);
//
//        Window window = dialog.getWindow();
//        window.setWindowAnimations(R.style.main_menu_animstyle);
//        WindowManager.LayoutParams wl = window.getAttributes();
//        wl.x = 0;
//        wl.y = getWindowManager().getDefaultDisplay().getHeight();
//        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
//        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//        dialog.onWindowAttributesChanged(wl);
//        dialog.setCanceledOnTouchOutside(true);
//        dialog.show();
//    }
    //填充ListView   3
    class dialogAdapter extends BaseAdapter {


        private ArrayList<HashMap<String, Object>> data;
        private MyView views;
        public dialogAdapter(ArrayList<HashMap<String, Object>> data){

            this.data=data;
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView = MainActivity.this.getLayoutInflater().inflate(R.layout.tasklist_item,null);
                views=new MyView();
                views.TaskID=(TextView)convertView.findViewById(R.id.TaskID);
                views.startPoint=(TextView)convertView.findViewById(R.id.startPoint);
                views.stopPoint=(TextView)convertView.findViewById(R.id.stopPoint);
                convertView.setTag(views);
            }else{
                views=(MyView)convertView.getTag();
            }
            final String TaskID = data.get(position).get("carTaskID").toString();
            final String startPoint = data.get(position).get("startPointNO").toString();
            final String stopPoint = data.get(position).get("endPointNO").toString();
            views.TaskID.setText(TaskID);
            views.startPoint.setText(startPoint);
            views.stopPoint.setText(stopPoint);
            //设置点击进入详情的点击事件
            views.TaskID.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplication(), "点击了任务", Toast.LENGTH_SHORT).show();
                    startQuery(startPoint, stopPoint);
                }

            });
            views.startPoint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplication(), "点击了任务", Toast.LENGTH_SHORT).show();
                    startQuery(startPoint, stopPoint);
                }

            });
            views.stopPoint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplication(), "点击了任务", Toast.LENGTH_SHORT).show();
                    startQuery(startPoint, stopPoint);
                }

            });

            return convertView;
        }
        class MyView{
            public TextView TaskID;
            public TextView startPoint;
            public TextView stopPoint;
        }
    }
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }
    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }
}
