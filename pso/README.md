実行方法
PS C:\Users\maedalab19a\Java> javac pso\*.java pso\core\*.java pso\model\*.java pso\util\*.java pso/visual/*.java
PS C:\Users\maedalab19a\Java> java -cp . pso.Main

(base) tanakarikuto@tanakarikuninnoMacBook-Air pso % javac pso/*.java pso/core/*.java pso/model/*.java pso/util/*.java pso/visual/*.java
(base) tanakarikuto@tanakarikuninnoMacBook-Air pso % java -cp . pso.Main

ディレクトリ構成図
pso-java/
pso/
├── Main.java                 // 実行エントリポイント
├── model/
│   ├── Particle.java         // 粒子クラス
│   └── Position.java         // 粒子の位置
├── core/
│   ├── PSO.java              // アルゴリズム制御
│   ├── FitnessFunction.java  // 評価関数インターフェース
│   └── Velocity.java         // 速度ベクトル管理
└── util/
    └── RandomUtils.java      // ランダム生成のユーティリティ

📁 pso/
🔹 Main.java（実行エントリポイント）
目的： アプリケーションの起動・PSOアルゴリズムの実行指示を出すクラス。

main() メソッドを持つ。

評価関数（例えば Sphere 関数）の定義。

PSO クラスに対して初期設定（粒子数、次元数、反復数、パラメータなど）を渡して実行。

やっていること：

実行パラメータの定義（粒子数、最大反復数、慣性重みなど）

PSOインスタンスの生成

アルゴリズムの実行：pso.run()

📁 pso/model/（データモデルを定義）
🔹 Particle.java（粒子クラス）
目的： PSOの「粒子」1個を表現。位置、速度、個人ベストなどを保持するデータクラス。

主なフィールド：

Position position：現在の位置ベクトル

Velocity velocity：現在の速度ベクトル

Position personalBest：最良位置（pBest）

double personalBestValue：pBest の評価値

やっていること：

粒子の情報を格納

new Particle(...) で初期化

🔹 Position.java（位置ベクトルクラス）
目的： 粒子の位置を表現するためのクラス。

やっていること：

多次元ベクトルとして double[] values を持つ

粒子の位置の読み書きをわかりやすくするための単純なラッパー

📁 pso/core/（アルゴリズムの核となる部分）
🔹 PSO.java（PSOアルゴリズム制御クラス）
目的： PSOの実装本体。全体の進化ループや、粒子の更新処理などを司る。

やっていること：

・初期化：粒子の生成、位置と速度の初期設定

・反復処理：
    ・速度更新
    ・位置更新
    ・個人ベスト（pBest）の更新
    ・グローバルベスト（gBest）の更新

・実行中に現在の最良評価値を出力

備考：

run() メソッドが実行ループを担当。

内部で updateVelocity(), updatePosition() など複数の補助メソッドを使って計算を分担。

🔹 FitnessFunction.java（評価関数インターフェース）
目的： 最適化対象（目的関数）を外部から差し替え可能にするためのインターフェース。

やっていること：

evaluate(Position position) メソッドだけを持つ。

これを実装することで、Sphere関数、Rastrigin関数などに柔軟に対応可能。

例：

java
コピーする
編集する
FitnessFunction sphere = pos -> {
    double sum = 0;
    for (double x : pos.values) {
        sum += x * x;
    }
    return sum;
};
🔹 Velocity.java（速度ベクトルクラス）
目的： 粒子の速度を管理するためのラッパークラス。

やっていること：

double[] values を保持

速度制限（min/max）や速度更新時に利用される

備考： Position.java とほぼ同様の構造

📁 pso/util/（ユーティリティ系）
🔹 RandomUtils.java（ランダム生成ユーティリティ）
目的： ランダムな数値生成を一元管理。

やっていること：

乱数生成のヘルパー：randomDouble(min, max)

毎回 new Random() せずに使えるようにする設計

利点：

テストしやすい（将来シード固定など可能）

可読性・再利用性の向上

PSOの詳細

🔁 実行の流れ順に解説（PSO.run() が起点）
① run()

public void run() {
    initializeSwarm(); // 🧩 Step 1: 粒子の初期化
② initializeSwarm()：粒子群を初期化

for (int i = 0; i < swarmSize; i++) {
    double[] position = new double[dimensions];
    double[] velocity = new double[dimensions];
→ 粒子ごとに dimensions 次元の位置と速度の配列を作成。

position[d] = RandomUtils.randomDouble(minPosition, maxPosition);
velocity[d] = RandomUtils.randomDouble(minVelocity, maxVelocity);
→ 🔗 [RandomUtils] を使ってランダムな初期位置・速度を生成。

Particle p = new Particle(new Position(position), new Velocity(velocity));
→ 🔗 [Position, Velocity, Particle] を使って粒子を作成。

double fitness = function.evaluate(p.position);
→ 🔗 [FitnessFunction] に位置を渡して目的関数の評価。

if (fitness < globalBestValue) {
    globalBestValue = fitness;
    globalBest = new Position(p.position.values.clone());
}
→ 初回なので、最も良い粒子の位置を「全体ベスト（global best）」として登録。

③ run() のループに戻る（メインイテレーション）

for (int iter = 0; iter < maxIterations; iter++) {
    for (Particle p : swarm) {
A. updateVelocity(p)：粒子の速度更新

double r1 = Math.random();
double r2 = Math.random();
→ 認知項と社会項に使う乱数を生成。

double cognitive = c1 * r1 * (p.personalBest.values[d] - p.position.values[d]);
double social = c2 * r2 * (globalBest.values[d] - p.position.values[d]);
→ 認知項（自分のベスト）と社会項（全体のベスト）で新しい方向を計算。

p.velocity.values[d] = w * p.velocity.values[d] + cognitive + social;
→ 慣性項 + 認知項 + 社会項 の合計が新しい速度。

p.velocity.values[d] = Math.max(minVelocity, Math.min(maxVelocity, ...));
→ 速度に制限をかけて暴走防止。

B. updatePosition(p)：位置を更新

p.position.values[d] += p.velocity.values[d];
→ 現在位置に新しい速度を加えて更新。

p.position.values[d] = Math.max(minPosition, Math.min(maxPosition, ...));
→ 移動先が許容範囲を超えていたら制限。

C. updatePersonalBest(p)：自己ベストの更新

double fitness = function.evaluate(p.position);
→ 🔗 目的関数で現在の位置を再評価。

if (fitness < p.personalBestValue) {
    p.personalBestValue = fitness;
    p.personalBest = new Position(p.position.values.clone());
}
→ 評価が前より良ければ、自己ベストを更新。

D. updateGlobalBest(p)：全体ベストの更新
if (p.personalBestValue < globalBestValue) {
    globalBestValue = p.personalBestValue;
    globalBest = new Position(p.personalBest.values.clone());
}
→ 自己ベストが群れ全体よりも良ければ、グローバルベストも更新。

④ System.out.println(...)：途中経過を表示
System.out.println("Iteration " + iter + ": Global Best Value = " + globalBestValue);
→ 現在の最良値を標準出力。収束の様子を確認できる。

🔁 以上の処理を maxIterations 回繰り返して最適解を探す。


=== PSO プログラム開始 ===
1. 評価関数（Sphere関数）を定義
2. PSOインスタンスを作成
    → PSOコンストラクタ開始
    → PSOコンストラクタ完了: 粒子数=3, 次元=2
3. PSOアルゴリズムを実行
    → PSO.run() 開始
    → Step 1: 粒子群の初期化
      → initializeSwarm() 開始
        → 粒子 0 の初期化
        → 初期位置: (-2.7484804001378045, -9.667798253958416)
        → 初期速度: (0.27328862177030966, -0.22315049784251761)
    → FitnessFunction.evaluate() 呼び出し: 位置 = (-2.7484804001378045, -9.667798253958416)
    → 評価値 = 101.02046758918307
        → 初期評価値: 101.02046758918307
        → 新しい全体ベスト: 101.02046758918307
        → 粒子 1 の初期化
        → 初期位置: (8.915939346332426, 1.5800339699512929)
        → 初期速度: (0.8652565152908023, 0.9939617420190907)
    → FitnessFunction.evaluate() 呼び出し: 位置 = (8.915939346332426, 1.5800339699512929)
    → 評価値 = 81.99048177367872
        → 初期評価値: 81.99048177367872
        → 新しい全体ベスト: 81.99048177367872
        → 粒子 2 の初期化
        → 初期位置: (-1.3143441498816486, 8.812588712994007)
        → 初期速度: (0.5406373279910284, -0.43282220163359986)
    → FitnessFunction.evaluate() 呼び出し: 位置 = (-1.3143441498816486, 8.812588712994007)
    → 評価値 = 79.38922036871747
        → 初期評価値: 79.38922036871747
        → 新しい全体ベスト: 79.38922036871747
      → initializeSwarm() 完了: 全体ベスト値 = 79.38922036871747
    → Step 2: メインループ開始 (3回)
    → 反復 0 開始
      → 粒子 0 の更新開始
        → updateVelocity() 開始
        → 次元 0: 速度 0.27328862177030966 → 0.4137168671165832
        → 次元 1: 速度 -0.22315049784251761 → 1.0
        → updateVelocity() 完了
        → updatePosition() 開始
        → 次元 0: 位置 -2.7484804001378045 → -2.3347635330212215
        → 次元 1: 位置 -9.667798253958416 → -8.667798253958416
        → updatePosition() 完了
        → updatePersonalBest() 開始
    → FitnessFunction.evaluate() 呼び出し: 位置 = (-2.3347635330212215, -8.667798253958416)
    → 評価値 = 80.58184732645032
        → 現在の評価値: 80.58184732645032 (個人ベスト: 101.02046758918307)
        → 個人ベスト更新: 80.58184732645032
        → updatePersonalBest() 完了
        → updateGlobalBest() 開始
        → 個人ベスト値: 80.58184732645032 (全体ベスト: 79.38922036871747)
        → updateGlobalBest() 完了
      → 粒子 0 の更新完了
      → 粒子 1 の更新開始
        → updateVelocity() 開始
        → 次元 0: 速度 0.8652565152908023 → -1.0
        → 次元 1: 速度 0.9939617420190907 → 1.0
        → updateVelocity() 完了
        → updatePosition() 開始
        → 次元 0: 位置 8.915939346332426 → 7.915939346332426
        → 次元 1: 位置 1.5800339699512929 → 2.580033969951293
        → updatePosition() 完了
        → updatePersonalBest() 開始
    → FitnessFunction.evaluate() 呼び出し: 位置 = (7.915939346332426, 2.580033969951293)
    → 評価値 = 69.31867102091645
        → 現在の評価値: 69.31867102091645 (個人ベスト: 81.99048177367872)
        → 個人ベスト更新: 69.31867102091645
        → updatePersonalBest() 完了
        → updateGlobalBest() 開始
        → 個人ベスト値: 69.31867102091645 (全体ベスト: 79.38922036871747)
        → 全体ベスト更新: 69.31867102091645
        → updateGlobalBest() 完了
      → 粒子 1 の更新完了
      → 粒子 2 の更新開始
        → updateVelocity() 開始
        → 次元 0: 速度 0.5406373279910284 → 1.0
        → 次元 1: 速度 -0.43282220163359986 → -0.30058892629030765
        → updateVelocity() 完了
        → updatePosition() 開始
        → 次元 0: 位置 -1.3143441498816486 → -0.3143441498816486
        → 次元 1: 位置 8.812588712994007 → 8.511999786703699
        → updatePosition() 完了
        → updatePersonalBest() 開始
    → FitnessFunction.evaluate() 呼び出し: 位置 = (-0.3143441498816486, 8.511999786703699)
    → 評価値 = 72.55295261340862
        → 現在の評価値: 72.55295261340862 (個人ベスト: 79.38922036871747)
        → 個人ベスト更新: 72.55295261340862
        → updatePersonalBest() 完了
        → updateGlobalBest() 開始
        → 個人ベスト値: 72.55295261340862 (全体ベスト: 69.31867102091645)
        → updateGlobalBest() 完了
      → 粒子 2 の更新完了
    → 反復 0 完了: Global Best Value = 69.31867102091645
    → 反復 1 開始
      → 粒子 0 の更新開始
        → updateVelocity() 開始
        → 次元 0: 速度 0.4137168671165832 → 1.0
        → 次元 1: 速度 1.0 → 1.0
        → updateVelocity() 完了
        → updatePosition() 開始
        → 次元 0: 位置 -2.3347635330212215 → -1.3347635330212215
        → 次元 1: 位置 -8.667798253958416 → -7.667798253958416
        → updatePosition() 完了
        → updatePersonalBest() 開始
    → FitnessFunction.evaluate() 呼び出し: 位置 = (-1.3347635330212215, -7.667798253958416)
    → 評価値 = 60.576723752491034
        → 現在の評価値: 60.576723752491034 (個人ベスト: 80.58184732645032)
        → 個人ベスト更新: 60.576723752491034
        → updatePersonalBest() 完了
        → updateGlobalBest() 開始
        → 個人ベスト値: 60.576723752491034 (全体ベスト: 69.31867102091645)
        → 全体ベスト更新: 60.576723752491034
        → updateGlobalBest() 完了
      → 粒子 0 の更新完了
      → 粒子 1 の更新開始
        → updateVelocity() 開始
        → 次元 0: 速度 -1.0 → -1.0
        → 次元 1: 速度 1.0 → -1.0
        → updateVelocity() 完了
        → updatePosition() 開始
        → 次元 0: 位置 7.915939346332426 → 6.915939346332426
        → 次元 1: 位置 2.580033969951293 → 1.5800339699512929
        → updatePosition() 完了
        → updatePersonalBest() 開始
    → FitnessFunction.evaluate() 呼び出し: 位置 = (6.915939346332426, 1.5800339699512929)
    → 評価値 = 50.32672438834902
        → 現在の評価値: 50.32672438834902 (個人ベスト: 69.31867102091645)
        → 個人ベスト更新: 50.32672438834902
        → updatePersonalBest() 完了
        → updateGlobalBest() 開始
        → 個人ベスト値: 50.32672438834902 (全体ベスト: 60.576723752491034)
        → 全体ベスト更新: 50.32672438834902
        → updateGlobalBest() 完了
      → 粒子 1 の更新完了
      → 粒子 2 の更新開始
        → updateVelocity() 開始
        → 次元 0: 速度 1.0 → 1.0
        → 次元 1: 速度 -0.30058892629030765 → -1.0
        → updateVelocity() 完了
        → updatePosition() 開始
        → 次元 0: 位置 -0.3143441498816486 → 0.6856558501183514
        → 次元 1: 位置 8.511999786703699 → 7.511999786703699
        → updatePosition() 完了
        → updatePersonalBest() 開始
    → FitnessFunction.evaluate() 呼び出し: 位置 = (0.6856558501183514, 7.511999786703699)
    → 評価値 = 56.90026474023794
        → 現在の評価値: 56.90026474023794 (個人ベスト: 72.55295261340862)
        → 個人ベスト更新: 56.90026474023794
        → updatePersonalBest() 完了
        → updateGlobalBest() 開始
        → 個人ベスト値: 56.90026474023794 (全体ベスト: 50.32672438834902)
        → updateGlobalBest() 完了
      → 粒子 2 の更新完了
    → 反復 1 完了: Global Best Value = 50.32672438834902
    → 反復 2 開始
      → 粒子 0 の更新開始
        → updateVelocity() 開始
        → 次元 0: 速度 1.0 → 1.0
        → 次元 1: 速度 1.0 → 1.0
        → updateVelocity() 完了
        → updatePosition() 開始
        → 次元 0: 位置 -1.3347635330212215 → -0.3347635330212215
        → 次元 1: 位置 -7.667798253958416 → -6.667798253958416
        → updatePosition() 完了
        → updatePersonalBest() 開始
    → FitnessFunction.evaluate() 呼び出し: 位置 = (-0.3347635330212215, -6.667798253958416)
    → 評価値 = 44.57160017853176
        → 現在の評価値: 44.57160017853176 (個人ベスト: 60.576723752491034)
        → 個人ベスト更新: 44.57160017853176
        → updatePersonalBest() 完了
        → updateGlobalBest() 開始
        → 個人ベスト値: 44.57160017853176 (全体ベスト: 50.32672438834902)
        → 全体ベスト更新: 44.57160017853176
        → updateGlobalBest() 完了
      → 粒子 0 の更新完了
      → 粒子 1 の更新開始
        → updateVelocity() 開始
        → 次元 0: 速度 -1.0 → -1.0
        → 次元 1: 速度 -1.0 → -1.0
        → updateVelocity() 完了
        → updatePosition() 開始
        → 次元 0: 位置 6.915939346332426 → 5.915939346332426
        → 次元 1: 位置 1.5800339699512929 → 0.5800339699512929
        → updatePosition() 完了
        → updatePersonalBest() 開始
    → FitnessFunction.evaluate() 呼び出し: 位置 = (5.915939346332426, 0.5800339699512929)
    → 評価値 = 35.33477775578159
        → 現在の評価値: 35.33477775578159 (個人ベスト: 50.32672438834902)
        → 個人ベスト更新: 35.33477775578159
        → updatePersonalBest() 完了
        → updateGlobalBest() 開始
        → 個人ベスト値: 35.33477775578159 (全体ベスト: 44.57160017853176)
        → 全体ベスト更新: 35.33477775578159
        → updateGlobalBest() 完了
      → 粒子 1 の更新完了
      → 粒子 2 の更新開始
        → updateVelocity() 開始
        → 次元 0: 速度 1.0 → 1.0
        → 次元 1: 速度 -1.0 → -1.0
        → updateVelocity() 完了
        → updatePosition() 開始
        → 次元 0: 位置 0.6856558501183514 → 1.6856558501183514
        → 次元 1: 位置 7.511999786703699 → 6.511999786703699
        → updatePosition() 完了
        → updatePersonalBest() 開始
    → FitnessFunction.evaluate() 呼び出し: 位置 = (1.6856558501183514, 6.511999786703699)
    → 評価値 = 45.247576867067245
        → 現在の評価値: 45.247576867067245 (個人ベスト: 56.90026474023794)
        → 個人ベスト更新: 45.247576867067245
        → updatePersonalBest() 完了
        → updateGlobalBest() 開始
        → 個人ベスト値: 45.247576867067245 (全体ベスト: 35.33477775578159)
        → updateGlobalBest() 完了
      → 粒子 2 の更新完了
    → 反復 2 完了: Global Best Value = 35.33477775578159
    → PSO.run() 完了
=== プログラム終了 ===