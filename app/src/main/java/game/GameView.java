package game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.example.test.R;

import java.util.Random;

public class GameView extends View {

    private static final int GRID_SIZE = 6;

    private int[][] board = new int[GRID_SIZE][GRID_SIZE];
    private int bearRow, bearCol;
    private int honeyRow, honeyCol;

    private Bitmap bg, bear, honey, rock, question;
    private Paint paint = new Paint();
    private Random random = new Random();

    private int cellSize;
    private boolean gameWon = false;

    public static final int TYPE_EMPTY = 0;
    public static final int TYPE_ROCK = 1;
    public static final int TYPE_QUESTION = 2;
    public static final int TYPE_HONEY = 3;

    private int numRows = 6, numCols = 6;
    private int[][] map;

    public GameView(Context context) {
        super(context);
        initBitmaps(context);
        initGame();
    }

    private void initBitmaps(Context ctx) {
        bg = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.background_game);
        bear = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.bear);
        honey = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.honey);
        rock = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.rock);
        question = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ques);

        // Tính toán kích thước mỗi ô dựa trên kích thước khung nền
        int cellWidth = bg.getWidth() / numCols;
        int cellHeight = bg.getHeight() / numRows;

// Scale các vật thể cho vừa khít từng ô
        bear = Bitmap.createScaledBitmap(bear, cellWidth, cellHeight, true);
        rock = Bitmap.createScaledBitmap(rock, cellWidth, cellHeight, true);
        question = Bitmap.createScaledBitmap(question, cellWidth, cellHeight, true);
        honey = Bitmap.createScaledBitmap(honey, cellWidth, cellHeight, true);


        // ✅ Khởi tạo bản đồ
        map = new int[numRows][numCols];

        // Thêm vật cản (1 = đá, 2 = dấu hỏi)
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                double rand = Math.random();
                if (rand < 0.15) map[i][j] = 1;        // 15% là đá
                else if (rand < 0.35) map[i][j] = 2;   // 20% là dấu hỏi
                else map[i][j] = 0;                    // 0 = đường trống
            }
        }

        // ✅ Đặt gấu vào vị trí bắt đầu
        bearRow = numRows / 2;
        bearCol = numCols / 2;
        map[bearRow][bearCol] = 0; // đảm bảo chỗ này trống
    }

    public void initGame() {
        // Tạo bản đồ ngẫu nhiên
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                board[i][j] = random.nextInt(10) < 7 ? TYPE_QUESTION : TYPE_ROCK;
            }
        }

        // Mật ong
        do {
            honeyRow = random.nextInt(GRID_SIZE);
            honeyCol = random.nextInt(GRID_SIZE);
        } while (board[honeyRow][honeyCol] == TYPE_ROCK);
        board[honeyRow][honeyCol] = TYPE_HONEY;

        // Gấu
        do {
            bearRow = random.nextInt(GRID_SIZE);
            bearCol = random.nextInt(GRID_SIZE);
        } while (board[bearRow][bearCol] == TYPE_ROCK || board[bearRow][bearCol] == TYPE_HONEY);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Kích thước mỗi ô
        cellSize = Math.min(width / numCols, height / numRows);

        // 🎨 Vẽ nền đồng cỏ
        Paint grassPaint = new Paint();
        grassPaint.setColor(0xFFA8E6A3); // xanh nhạt
        canvas.drawRect(0, 0, numCols * cellSize, numRows * cellSize, grassPaint);

        // 🎨 Vẽ các ô lưới
        Paint gridPaint = new Paint();
        gridPaint.setColor(0xFF66BB6A); // xanh đậm
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(3);

        for (int i = 0; i <= numCols; i++) {
            canvas.drawLine(i * cellSize, 0, i * cellSize, numRows * cellSize, gridPaint);
        }
        for (int j = 0; j <= numRows; j++) {
            canvas.drawLine(0, j * cellSize, numCols * cellSize, j * cellSize, gridPaint);
        }

        // 🌳 Vẽ viền cây cối xung quanh
        Paint treePaint = new Paint();
        treePaint.setColor(0xFF2E7D32); // xanh rừng
        for (int i = 0; i < numCols; i++) {
            // hàng trên
            canvas.drawRect(i * cellSize, 0, (i + 1) * cellSize, cellSize / 3, treePaint);
            // hàng dưới
            canvas.drawRect(i * cellSize, (numRows - 1) * cellSize + cellSize * 2 / 3,
                    (i + 1) * cellSize, numRows * cellSize, treePaint);
        }
        for (int j = 0; j < numRows; j++) {
            // cột trái
            canvas.drawRect(0, j * cellSize, cellSize / 3, (j + 1) * cellSize, treePaint);
            // cột phải
            canvas.drawRect((numCols - 1) * cellSize + cellSize * 2 / 3, j * cellSize,
                    numCols * cellSize, (j + 1) * cellSize, treePaint);
        }

        // 🪨 Vẽ vật thể trong bản đồ
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                float x = col * cellSize;
                float y = row * cellSize;

                if (map[row][col] == 1) {
                    canvas.drawBitmap(Bitmap.createScaledBitmap(rock, cellSize, cellSize, false), x, y, null);
                } else if (map[row][col] == 2) {
                    canvas.drawBitmap(Bitmap.createScaledBitmap(question, cellSize, cellSize, false), x, y, null);
                }
            }
        }

        // 🐻 Vẽ gấu
        canvas.drawBitmap(
                Bitmap.createScaledBitmap(bear, (int)(cellSize*0.9), (int)(cellSize*0.9), false),
                bearCol * cellSize + cellSize*0.05f,
                bearRow * cellSize + cellSize*0.05f,
                null
        );
    }


    // Di chuyển gấu
    public void moveBear(int dr, int dc) {
        if (gameWon) return;

        int nr = bearRow + dr;
        int nc = bearCol + dc;
        if (nr < 0 || nc < 0 || nr >= GRID_SIZE || nc >= GRID_SIZE) return;
        if (board[nr][nc] == TYPE_ROCK) return;

        bearRow = nr;
        bearCol = nc;

        if (board[nr][nc] == TYPE_HONEY) {
            gameWon = true;
            // bạn có thể gọi callback về Activity ở đây
        }

        // Nếu là ô dấu hỏi, sau khi đi qua thì trống
        if (board[nr][nc] == TYPE_QUESTION)
            board[nr][nc] = TYPE_EMPTY;

        invalidate(); // Vẽ lại
    }
}
