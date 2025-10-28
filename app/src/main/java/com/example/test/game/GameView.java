package com.example.test.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
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
    private int gridPixelWidth; // Tổng chiều rộng lưới game tính bằng pixel
    private int gridPixelHeight; // Tổng chiều cao lưới game tính bằng pixel
    private int startX; // Tọa độ X bắt đầu vẽ lưới game
    private int startY; // Tọa độ Y bắt đầu vẽ lưới game

    private Paint paintGrass, paintGrid, paintTree;
    private int[][] map;
    private int bearRow, bearCol;
    private int prevBearRow, prevBearCol;
    private int honeyRow, honeyCol;
    private boolean gameWon = false;
    private boolean gameOver = false;
    private boolean gameRunning = false;

    private Bitmap bear, rock, question, honey;
    private OnQuestionListener listener;
    private OnWinListener winListener;
    private OnGameOverListener gameOverListener;

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

    public interface OnGameOverListener {
        void onGameOver();
    }

    public void setOnQuestionListener(OnQuestionListener listener) {
        this.listener = listener;
    }

    public void setOnWinListener(OnWinListener listener) {
        this.winListener = listener;
    }

    public void setOnGameOverListener(OnGameOverListener listener) {
        this.gameOverListener = listener;
    }

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        initBitmaps(context);
        initPaints();
        setFocusable(true);
        setFocusableInTouchMode(true);

        // initMap sẽ được gọi sau khi SurfaceView có kích thước, thường trong surfaceCreated
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        initBitmaps(context);
        initPaints();
        setFocusable(true);
        setFocusableInTouchMode(true);

        // initMap sẽ được gọi sau khi SurfaceView có kích thước, thường trong surfaceCreated
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

        Log.d("DEBUG_BITMAP", "bear = " + (bear != null));
        Log.d("DEBUG_BITMAP", "rock = " + (rock != null));
        Log.d("DEBUG_BITMAP", "question = " + (question != null));
        Log.d("DEBUG_BITMAP", "honey = " + (honey != null));

    }

    private void initMap() {
        bearRow = numRows / 2;
        bearCol = numCols / 2;
        prevBearRow = bearRow;
        prevBearCol = bearCol;
        gameWon = false;
        gameOver = false;
        gameRunning = true; // Bắt đầu game

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
                    if (!visited[nr][nc]) {
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
        // Gọi initMap ở đây để đảm bảo kích thước view đã có
        Log.d("GameView", "surfaceCreated() called. Initializing map and drawing.");
        initMap();
        drawGame(holder);
    }

    public void moveBear(int dr, int dc) {
        if (!gameRunning) return;
        if (gameWon || gameOver) return;

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
            gameRunning = false;
            if (winListener != null) winListener.onGameWon();
        }

        drawGame(getHolder());
    }

    public void clearQuestionAt(int row, int col) {
        map[row][col] = TYPE_EMPTY;
        drawGame(getHolder());
    }

    public void handleWrongAnswer(int questionRow, int questionCol) {
        if (!gameRunning) return;
        if (gameWon || gameOver) return;

        if (map[questionRow][questionCol] == TYPE_QUESTION) {
            map[questionRow][questionCol] = TYPE_ROCK;
        }

        bearRow = prevBearRow;
        bearCol = prevBearCol;

        if (findPathAndQuestionCount(bearRow, bearCol, honeyRow, honeyCol) == -1) {
            gameOver = true;
            gameRunning = false;
            if (gameOverListener != null) {
                gameOverListener.onGameOver();
            }
        }

        drawGame(getHolder());
    }

    private void calculateDrawingMetrics(int canvasWidth, int canvasHeight) {
        // numRows và numCols là kích thước lưới game thực tế (6x6).
        // Chúng ta có thêm 2 hàng/cột cho viền cây, nên tổng là (numRows + 2) x (numCols + 2) ô hiển thị.
        int totalDisplayRows = numRows + 2;
        int totalDisplayCols = numCols + 2;

        // Tính toán kích thước ô dựa trên chiều rộng và chiều cao của canvas.
        // Chọn kích thước ô nhỏ nhất để đảm bảo toàn bộ lưới game vừa với GameView.
        cellSize = Math.min(canvasWidth / totalDisplayCols, canvasHeight / totalDisplayRows);

        // Tổng chiều rộng và chiều cao của lưới game (bao gồm viền cây) tính bằng pixel.
        gridPixelWidth = totalDisplayCols * cellSize;
        gridPixelHeight = totalDisplayRows * cellSize;

        // Tính toán tọa độ X, Y bắt đầu để căn giữa lưới game trên canvas.
        startX = (canvasWidth - gridPixelWidth) / 2;
        startY = (canvasHeight - gridPixelHeight) / 2;
    }


    private void drawGame(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            Log.e("GameView", "drawGame(): Canvas is null!");
            return;
        }


        // Lấy kích thước canvas (kích thước của GameView)
        int viewWidth = canvas.getWidth();
        int viewHeight = canvas.getHeight();
        Log.d("GameView", "drawGame(): Canvas Width=" + viewWidth + ", Height=" + viewHeight);
        // Tính toán lại các thông số vẽ mỗi khi vẽ (đảm bảo thích ứng với thay đổi kích thước)
        calculateDrawingMetrics(viewWidth, viewHeight);

        // 1. Vẽ toàn bộ nền của GameView
        canvas.drawColor(Color.rgb(180, 255, 180)); // Màu nền của GameView
        // Hoặc có thể vẽ một hình chữ nhật lớn với màu này:
        // canvas.drawRect(0, 0, viewWidth, viewHeight, paintGrass);

        // 2. Vẽ viền cây
        for (int row = 0; row < numRows + 2; row++) {
            for (int col = 0; col < numCols + 2; col++) {
                if (row == 0 || col == 0 || row == numRows + 1 || col == numCols + 1) {
                    // Tọa độ X, Y của tâm hình tròn cây, được offset bởi startX, startY
                    float cx = startX + col * cellSize + cellSize / 2f;
                    float cy = startY + row * cellSize + cellSize / 2f;
                    canvas.drawCircle(cx, cy, cellSize / 2.5f, paintTree);
                }
            }
        }

        // 3. Vẽ bản đồ (đá, dấu hỏi) và lưới
        // Các ô bản đồ thực tế bắt đầu từ hàng 1, cột 1 (sau viền cây)
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                // Tọa độ X, Y của góc trên bên trái của ô, offset bởi startX, startY
                // +1 là để bỏ qua hàng/cột viền cây
                float x = startX + (c + 1) * cellSize;
                float y = startY + (r + 1) * cellSize;

                // Vẽ nền màu cỏ cho ô game
                canvas.drawRect(x, y, x + cellSize, y + cellSize, paintGrass);

                Bitmap obj = null;
                if (map[r][c] == TYPE_ROCK) obj = rock;
                else if (map[r][c] == TYPE_QUESTION) obj = question;

                if (obj != null) {
                    // Scale bitmap để vừa với kích thước ô
                    Bitmap scaled = Bitmap.createScaledBitmap(obj, cellSize, cellSize, true);
                    canvas.drawBitmap(scaled, x, y, null);
                }

                // Vẽ đường lưới cho ô
                canvas.drawRect(x, y, x + cellSize, y + cellSize, paintGrid);
            }
        }

        // 4. Vẽ hũ mật
        // (honeyCol + 1) và (honeyRow + 1) để căn chỉnh với lưới game thực tế
        float honeyX = startX + (honeyCol + 1) * cellSize;
        float honeyY = startY + (honeyRow + 1) * cellSize;
        Bitmap honeyScaled = Bitmap.createScaledBitmap(honey, cellSize, cellSize, true);
        canvas.drawBitmap(honeyScaled, honeyX, honeyY, null);

        // 5. Vẽ gấu
        // (bearCol + 1) và (bearRow + 1) để căn chỉnh với lưới game thực tế
        float bearX = startX + (bearCol + 1) * cellSize;
        float bearY = startY + (bearRow + 1) * cellSize;
        Bitmap bearScaled = Bitmap.createScaledBitmap(bear, cellSize, cellSize, true);
        canvas.drawBitmap(bearScaled, bearX, bearY, null);

        holder.unlockCanvasAndPost(canvas);
        Log.d("GameView", "drawGame(): Canvas unlocked and posted.");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Kích thước của SurfaceView đã thay đổi, cần vẽ lại
        drawGame(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    public void resetGame() {
        initMap(); // Khởi tạo lại bản đồ, gấu, mật, và đặt gameRunning = true
        drawGame(getHolder());
    }

    public void setGameRunning(boolean running) {
        this.gameRunning = running;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isGameWon() {
        return gameWon;
    }
}