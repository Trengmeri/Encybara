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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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

    //private int bearRow, bearCol;
    private int prevBearRow, prevBearCol;

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
        this.winListener  = listener;
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
        bearRow = numRows / 2;
        bearCol = numCols / 2;
        prevBearRow = bearRow;
        prevBearCol = bearCol;
        int minQuestions = 5;
        boolean validMap = false;

        while (!validMap) {
            map = new int[numRows][numCols];
            // Khởi tạo map với đá và câu hỏi ngẫu nhiên
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    map[r][c] = TYPE_EMPTY; // Mặc định là ô trống
                    if (r == bearRow && c == bearCol) continue; // Không đặt gì ở vị trí gấu

                    double rand = Math.random();
                    if (rand < 0.20) map[r][c] = TYPE_ROCK; // Tăng tỉ lệ đá để tạo đường đi khó hơn
                    else if (rand < 0.45) map[r][c] = TYPE_QUESTION; // Tăng tỉ lệ câu hỏi
                }
            }

            // Đặt hũ mật ngẫu nhiên, không trùng đá hoặc gấu
            do {
                honeyRow = random.nextInt(numRows);
                honeyCol = random.nextInt(numCols);
            } while (map[honeyRow][honeyCol] == TYPE_ROCK ||
                    (honeyRow == bearRow && honeyCol == bearCol));

            // Đảm bảo vị trí hũ mật không phải là câu hỏi ban đầu để người chơi phải đi qua các câu hỏi khác
            map[honeyRow][honeyCol] = TYPE_EMPTY;

            // Kiểm tra đường đi có hợp lệ và số câu hỏi tối thiểu
            if (isValidPath(bearRow, bearCol, honeyRow, honeyCol, minQuestions)) {
                validMap = true;
            } else {
                // Nếu không hợp lệ, thử lại với một bản đồ mới
                // Có thể điều chỉnh số lượng đá/câu hỏi hoặc vị trí khởi tạo để tăng khả năng tìm được bản đồ hợp lệ
            }
        }
    }

    private boolean isValidPath(int startR, int startC, int targetR, int targetC, int minQuestions) {
        // Sử dụng BFS để tìm đường đi ngắn nhất và đếm số câu hỏi trên đường đi
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[numRows][numCols];
        int[][] questionCount = new int[numRows][numCols]; // Số câu hỏi đã gặp trên đường đến ô này

        queue.offer(new int[]{startR, startC});
        visited[startR][startC] = true;
        questionCount[startR][startC] = (map[startR][startC] == TYPE_QUESTION ? 1 : 0);

        int[] dr = {-1, 1, 0, 0}; // Lên, xuống
        int[] dc = {0, 0, -1, 1}; // Trái, phải

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];

            if (r == targetR && c == targetC) {
                return questionCount[r][c] >= minQuestions;
            }

            for (int i = 0; i < 4; i++) {
                int nr = r + dr[i];
                int nc = c + dc[i];

                if (nr >= 0 && nr < numRows && nc >= 0 && nc < numCols && !visited[nr][nc] && map[nr][nc] != TYPE_ROCK) {
                    visited[nr][nc] = true;
                    int newQuestionCount = questionCount[r][c] + (map[nr][nc] == TYPE_QUESTION ? 1 : 0);
                    questionCount[nr][nc] = newQuestionCount;
                    queue.offer(new int[]{nr, nc});
                }
            }
        }
        return false; // Không tìm thấy đường đi đến hũ mật
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

        // ✅ Lưu vị trí hiện tại làm vị trí trước đó
        prevBearRow = bearRow;
        prevBearCol = bearCol;

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

    // ✅ Phương thức được gọi khi người chơi trả lời sai câu hỏi
    public void handleWrongAnswer(int questionRow, int questionCol) {
        // Biến ô câu hỏi thành đá
        if (map[questionRow][questionCol] == TYPE_QUESTION) {
            map[questionRow][questionCol] = TYPE_ROCK;
        }

        // Đẩy gấu về ô trước đó
        bearRow = prevBearRow;
        bearCol = prevBearCol;

        drawGame(getHolder());
    }
}