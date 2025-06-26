実行方法
PS C:\Users\maedalab19a\Java> javac pso\*.java pso\core\*.java pso\model\*.java pso\util\*.java
PS C:\Users\maedalab19a\Java> java -cp . pso.Main

(base) tanakarikuto@tanakarikuninnoMacBook-Air pso % javac pso/*.java pso/core/*.java pso/model/*.java pso/util/*.java
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