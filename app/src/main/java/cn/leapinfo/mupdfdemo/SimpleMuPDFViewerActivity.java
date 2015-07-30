package cn.leapinfo.mupdfdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdflib.FilePicker;
import com.artifex.mupdflib.MuPDFCore;
import com.artifex.mupdflib.MuPDFPageAdapter;
import com.artifex.mupdflib.MuPDFReaderView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SimpleMuPDFViewerActivity extends AppCompatActivity implements FilePicker.FilePickerSupport {
    private static final String TAG = SimpleMuPDFViewerActivity.class.getSimpleName();

    @Bind(R.id.toolbar_title)
    TextView mTitle;

    @Bind(R.id.toolbar_with_back_title)
    Toolbar mToolbar;

    private boolean isFinished;

    int lastPagePosition;
    String mFileName;
    int totalPageCount;
    String path;

    private MuPDFCore core;

    @Bind(R.id.pdf_doc_view_container)
    RelativeLayout mPdfContainer;

    private MuPDFReaderView mDocView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_mu_pdfviewer);
        ButterKnife.bind(this);
        isFinished = false;
        path =  getIntent().getStringExtra("FILE_PATH");
        lastPagePosition = getIntent().getIntExtra("LAST_POSITION",0);
        if (path == null ) {
            Toast.makeText(this, "parameter error", Toast.LENGTH_SHORT).show();
            finish();
        }

        initToolbar();

        initPdfView();
    }

    private void initPdfView() {
        core = openFile(path);
        if(core==null){
            Toast.makeText(this,"open pdf file failed",Toast.LENGTH_SHORT).show();
            finish();
        }
        totalPageCount = core.countPages();
        if(lastPagePosition>totalPageCount){
            lastPagePosition=totalPageCount;
            isFinished=true;
        }
        if (totalPageCount == 0) {
            Toast.makeText(this, "PDF file has format error", Toast.LENGTH_SHORT).show();
            finish();
        }
        //one page per screen
        core.setDisplayPages(1);

        mDocView = new MuPDFReaderView(this) {
            @Override
            protected void onMoveToChild(int i) {
                Log.d(TAG, "onMoveToChild " + i);
                super.onMoveToChild(i);
                mTitle.setText(String.format(" %s / %s ",  i + 1, totalPageCount));
                if ((i + 1) == totalPageCount) {
                    isFinished = true;
                }
            }

            @Override
            protected void onTapMainDocArea() {
                //Log.d(TAG,"onTapMainDocArea");
            }

            @Override
            protected void onDocMotion() {
                //Log.d(TAG,"onDocMotion");
            }

        };
        mDocView.setAdapter(new MuPDFPageAdapter(this, this, core));
        mDocView.setKeepScreenOn(true);
        mDocView.setLinksHighlighted(false);
        mDocView.setScrollingDirectionHorizontal(true);

        mPdfContainer.addView(mDocView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDocView.setDisplayedViewIndex(lastPagePosition - 1);
        mTitle.setText(String.format(" %s / %s ", lastPagePosition, totalPageCount));
    }

    @OnClick(R.id.image_view_back)
    public void back() {
        finish();
    }

    @Override
    public void finish() {
        int currentPosition = mDocView!=null ? mDocView.getDisplayedViewIndex()+1:lastPagePosition;
        if (isFinished) {
            currentPosition = totalPageCount;
        }

        super.finish();
    }

    private void initToolbar() {
        mTitle.setText("PDF reader");
        setSupportActionBar(mToolbar);
    }

    private MuPDFCore openFile(String path) {
        int lastSlashPos = path.lastIndexOf('/');
        mFileName = lastSlashPos == -1 ? path
                : path.substring(lastSlashPos + 1);
        System.out.println("Trying to open " + path);
        try {
            core = new MuPDFCore(this, path);
            // New file: drop the old outline data
            //OutlineActivityData.set(null);
//            PDFPreviewGridActivityData.set(null);
        } catch (Exception e) {
            System.out.println(e);
            Log.e(TAG,e.getMessage());
            return null;
        }
        return core;
    }

    @Override
    public void performPickFor(FilePicker picker) {

    }
}
