package game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.test.R;

import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private int numRows = 6, numCols = 6;
    private int cellSize;
    private Paint paintGrass, paintGrid, paintTree;
    private int[][] map;
    private int bearRow, bearCol;
    private int honeyRow, honeyCol; // ✅ vị trí hũ mật
    private boolean gameWon = false;

    private Bitmap bear, rock, question, honey;
    private OnQuestionListener listener; // 👈 interface callback
    private OnWinListener winListener;   // 👈 callback khi thắng

    private Random random = new Random();

    public static final int TYPE_EMPTY = 0;
    public static final int TYPE_ROCK = 1;
    public static final int TYPE_QUESTION = 2;

    // Giao diện callback cho sự kiện câu hỏi
    public interface OnQuestionListener {
        void onQuestionTriggered(int row, int col);
    }

    public interface OnWinListener {
        void onGameWon();
    }

    public void setOnQuestionListener(OnQuestionListener listener) {
        this.listener = listener;
    }

    public void setOnWinListener(OnWinListener listener) {
        this.winListener = listener;
    }

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        initBitmaps(context);
        initPaints();
        initMap();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        initBitmaps(context);
        initPaints();
        initMap();
    }

    private void initPaints() {
        paintGrass = new Paint();
        paintGrass.setColor(Color.rgb(180, 255, 180));

        paintGrid = new Paint();
        paintGrid.setColor(Color.rgb(100, 180, 100));
        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setStrokeWidth(3);

        paintTree = new Paint();
        paintTree.setColor(Color.rgb(34, 139, 34));
    }

    private void initBitmaps(Context ctx) {
        bear = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.bear);
        rock = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.rock);
        question = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ques);
        honey = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.honey); // ✅ thêm hũ mật
    }

    private void initMap() {
        map = new int[numRows][numCols];
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                double rand = Math.random();
                if (rand < 0.15) map[r][c] = TYPE_ROCK;
                else if (rand < 0.35) map[r][c] = TYPE_QUESTION;
                else map[r][c] = TYPE_EMPTY;
            }
        }

        // ✅ Đặt gấu giữa bản đồ
        bearRow = numRows / 2;
        bearCol = numCols / 2;

        // ✅ Đặt hũ mật ngẫu nhiên, không trùng đá hoặc gấu
        do {
            honeyRow = random.nextInt(numRows);
            honeyCol = random.nextInt(numCols);
        } while (map[honeyRow][honeyCol] == TYPE_ROCK ||
                (honeyRow == bearRow && honeyCol == bearCol));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawGame(holder);
    }

    public void moveBear(int dr, int dc) {
        if (gameWon) return;

        int nr = bearRow + dr;
        int nc = bearCol + dc;

        if (nr < 0 || nr >= numRows || nc < 0 || nc >= numCols) return;
        if (map[nr][nc] == TYPE_ROCK) return;

        bearRow = nr;
        bearCol = nc;

        if (map[nr][nc] == TYPE_QUESTION && listener != null) {
            listener.onQuestionTriggered(nr, nc);
        }

        // ✅ Kiểm tra thắng
        if (bearRow == honeyRow && bearCol == honeyCol) {
            gameWon = true;
            if (winListener != null) winListener.onGameWon();
        }

        drawGame(getHolder());
    }

    public void clearQuestionAt(int row, int col) {
        map[row][col] = TYPE_EMPTY;
        drawGame(getHolder());
    }

    private void drawGame(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) return;

        int width = canvas.getWidth();
        int height = canvas.getHeight();
        cellSize = Math.min(width, height) / (numRows + 2);

        canvas.drawRect(0, 0, width, height, paintGrass);

        // Viền cây
        for (int row = 0; row < numRows + 2; row++) {
            for (int col = 0; col < numCols + 2; col++) {
                if (row == 0 || col == 0 || row == numRows + 1 || col == numCols + 1) {
                    float cx = col * cellSize + cellSize / 2f;
                    float cy = row * cellSize + cellSize / 2f;
                    canvas.drawCircle(cx, cy, cellSize / 2.5f, paintTree);
                }
            }
        }

        // Vẽ bản đồ (đá, dấu hỏi)
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                float x = (c + 1) * cellSize;
                float y = (r + 1) * cellSize;

                Bitmap obj = null;
                if (map[r][c] == TYPE_ROCK) obj = rock;
                else if (map[r][c] == TYPE_QUESTION) obj = question;

                if (obj != null) {
                    Bitmap scaled = Bitmap.createScaledBitmap(obj, cellSize, cellSize, true);
                    canvas.drawBitmap(scaled, x, y, null);
                }

                canvas.drawRect(x, y, x + cellSize, y + cellSize, paintGrid);
            }
        }

        // ✅ Vẽ hũ mật
        float honeyX = (honeyCol + 1) * cellSize;
        float honeyY = (honeyRow + 1) * cellSize;
        Bitmap honeyScaled = Bitmap.createScaledBitmap(honey, cellSize, cellSize, true);
        canvas.drawBitmap(honeyScaled, honeyX, honeyY, null);

        // ✅ Vẽ gấu
        float bearX = (bearCol + 1) * cellSize;
        float bearY = (bearRow + 1) * cellSize;
        Bitmap bearScaled = Bitmap.createScaledBitmap(bear, cellSize, cellSize, true);
        canvas.drawBitmap(bearScaled, bearX, bearY, null);

        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}
}
