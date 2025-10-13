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
    private int prevBearRow, prevBearCol;
    private int honeyRow, honeyCol;
    private boolean gameWon = false;
    private boolean gameOver = false; // ✅ Thêm cờ game over

    private Bitmap bear, rock, question, honey;
    private OnQuestionListener listener;
    private OnWinListener winListener;
    private OnGameOverListener gameOverListener; // ✅ Thêm listener cho Game Over

    private Random random = new Random();

    public static final int TYPE_EMPTY = 0;
    public static final int TYPE_ROCK = 1;
    public static final int TYPE_QUESTION = 2;

    public interface OnQuestionListener {
        void onQuestionTriggered(int row, int col);
    }

    public interface OnWinListener {
        void onGameWon();
    }

    // ✅ Giao diện callback cho sự kiện Game Over
    public interface OnGameOverListener {
        void onGameOver();
    }

    public void setOnQuestionListener(OnQuestionListener listener) {
        this.listener = listener;
    }

    public void setOnWinListener(OnWinListener listener) {
        this.winListener = listener;
    }

    // ✅ Setter cho OnGameOverListener
    public void setOnGameOverListener(OnGameOverListener listener) {
        this.gameOverListener = listener;
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
        honey = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.honey);
    }

    private void initMap() {
        bearRow = numRows / 2;
        bearCol = numCols / 2;
        prevBearRow = bearRow;
        prevBearCol = bearCol;
        gameWon = false;
        gameOver = false; // ✅ Đảm bảo reset cờ game over khi khởi tạo map

        int minQuestions = 5;
        boolean validMap = false;

        while (!validMap) {
            map = new int[numRows][numCols];
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    map[r][c] = TYPE_EMPTY;
                }
            }

            List<int[]> availableCells = new ArrayList<>();
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    if (r == bearRow && c == bearCol) continue;
                    availableCells.add(new int[]{r, c});
                }
            }
            Collections.shuffle(availableCells, random);

            int[] honeyCell = availableCells.remove(0);
            honeyRow = honeyCell[0];
            honeyCol = honeyCell[1];

            int rocksToPlace = (int) (numRows * numCols * 0.20);
            int questionsToPlace = (int) (numRows * numCols * 0.45);

            for (int[] cell : availableCells) {
                int r = cell[0];
                int c = cell[1];

                if (rocksToPlace > 0) {
                    map[r][c] = TYPE_ROCK;
                    rocksToPlace--;
                } else if (questionsToPlace > 0) {
                    map[r][c] = TYPE_QUESTION;
                    questionsToPlace--;
                } else {
                    break;
                }
            }

            map[bearRow][bearCol] = TYPE_EMPTY;
            map[honeyRow][honeyCol] = TYPE_EMPTY;

            if (findPathAndQuestionCount(bearRow, bearCol, honeyRow, honeyCol) >= minQuestions) {
                validMap = true;
            }
        }
    }

    // Hàm này sẽ trả về số câu hỏi trên đường đi ngắn nhất, hoặc -1 nếu không có đường đi.
    // Dùng BFS để tìm đường đi ngắn nhất và đếm số câu hỏi.
    private int findPathAndQuestionCount(int startR, int startC, int targetR, int targetC) {
        Queue<int[]> queue = new LinkedList<>();
        int[][] visitedQuestions = new int[numRows][numCols];
        boolean[][] visited = new boolean[numRows][numCols];

        for(int r=0; r<numRows; r++) {
            for(int c=0; c<numCols; c++) {
                visitedQuestions[r][c] = -1;
            }
        }

        queue.offer(new int[]{startR, startC});
        visited[startR][startC] = true;
        visitedQuestions[startR][startC] = (map[startR][startC] == TYPE_QUESTION ? 1 : 0);

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];

            if (r == targetR && c == targetC) {
                return visitedQuestions[r][c];
            }

            for (int i = 0; i < 4; i++) {
                int nr = r + dr[i];
                int nc = c + dc[i];

                if (nr >= 0 && nr < numRows && nc >= 0 && nc < numCols && map[nr][nc] != TYPE_ROCK) {
                    // Check if already visited or if we found a path with fewer questions
                    // For finding path existence, a simple visited check is enough
                    if (!visited[nr][nc]) { // Ensure we don't revisit in the same BFS path
                        visited[nr][nc] = true;
                        visitedQuestions[nr][nc] = visitedQuestions[r][c] + (map[nr][nc] == TYPE_QUESTION ? 1 : 0);
                        queue.offer(new int[]{nr, nc});
                    }
                }
            }
        }
        return -1;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawGame(holder);
    }

    public void moveBear(int dr, int dc) {
        if (gameWon || gameOver) return; // ✅ Thêm kiểm tra gameOver

        int nr = bearRow + dr;
        int nc = bearCol + dc;

        if (nr < 0 || nr >= numRows || nc < 0 || nc >= numCols) return;
        if (map[nr][nc] == TYPE_ROCK) return;

        prevBearRow = bearRow;
        prevBearCol = bearCol;

        bearRow = nr;
        bearCol = nc;

        if (map[nr][nc] == TYPE_QUESTION && listener != null) {
            listener.onQuestionTriggered(nr, nc);
        }

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

    // ✅ Cập nhật phương thức handleWrongAnswer
    public void handleWrongAnswer(int questionRow, int questionCol) {
        if (gameWon || gameOver) return; // ✅ Đảm bảo không xử lý nếu game đã kết thúc

        // Biến ô câu hỏi thành đá
        if (map[questionRow][questionCol] == TYPE_QUESTION) {
            map[questionRow][questionCol] = TYPE_ROCK;
        }

        // Đẩy gấu về ô trước đó
        bearRow = prevBearRow;
        bearCol = prevBearCol;

        // ✅ KIỂM TRA ĐƯỜNG ĐI SAU KHI Ô BỊ CHẶN
        // findPathAndQuestionCount sẽ trả về -1 nếu không còn đường đi.
        if (findPathAndQuestionCount(bearRow, bearCol, honeyRow, honeyCol) == -1) {
            gameOver = true; // ✅ Đặt cờ game over
            if (gameOverListener != null) {
                gameOverListener.onGameOver(); // ✅ Gọi callback thông báo game over
            }
        }

        drawGame(getHolder());
    }

    private void drawGame(SurfaceHolder holder) {
        // ... giữ nguyên ...
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

        // Vẽ hũ mật
        float honeyX = (honeyCol + 1) * cellSize;
        float honeyY = (honeyRow + 1) * cellSize;
        Bitmap honeyScaled = Bitmap.createScaledBitmap(honey, cellSize, cellSize, true);
        canvas.drawBitmap(honeyScaled, honeyX, honeyY, null);

        // Vẽ gấu
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

    // ✅ Phương thức để reset trạng thái game
    public void resetGame() {
        initMap(); // Khởi tạo lại bản đồ, gấu, mật
        drawGame(getHolder());
    }
}