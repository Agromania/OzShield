(ns heraldry.math.svg.path
  (:require ["svg-path-parse" :as svg-path-parse]
            ["svg-path-properties" :as svg-path-properties]
            ["svg-path-reverse" :as svg-path-reverse]
            ["svgpath" :as svgpath]
            [clojure.string :as s]
            [heraldry.math.vector :as v]))

(defn stitch [path]
  ;; TODO: this can be improved, it already broke some things and caused unexpected behaviour,
  ;; because the 'e' was not part of the pattern
  (s/replace path #"^M[ ]*[0-9.e-]+[, -] *[0-9.e-]+" ""))

(defn make-path [v]
  (cond
    (string? v) v
    (and (map? v)
         (:x v)
         (:y v)) (v/->str v)
    (sequential? v) (s/join " " (map make-path v))
    :else (str v)))

(defn translate [path dx dy]
  (-> path
      svgpath
      (.translate dx dy)
      .toString))

(defn reverse-path [path]
  (-> path
      svg-path-reverse/reverse
      svg-path-parse/pathParse
      .relNormalize
      (js->clj :keywordize-keys true)
      (as-> path
            (let [[move & rest] (:segments path)
                  [x y] (:args move)
                  adjusted-path (assoc path :segments (into [{:type "M" :args [0 0]}] rest))]
              {:start (v/v x y)
               :path (-> adjusted-path
                         clj->js
                         svg-path-parse/serializePath)}))))

(defn normalize-path-relative [path]
  (-> path
      svg-path-reverse/reverse
      svg-path-reverse/reverse
      svg-path-parse/pathParse
      .relNormalize
      svg-path-parse/serializePath))

(defn move-to [p]
  (str "M" (v/->str p)))

(defn line-to [p]
  (str " l" (v/->str p) " "))

(defn bezier-to-relative [[p1 cp1 cp2 p2]]
  (str "c" (v/->str (v/sub cp1 p1)) "," (v/->str (v/sub cp2 p1)) "," (v/->str (v/sub p2 p1))))

(defn curve-to-relative [curve]
  (let [start (first (first curve))]
    (s/join "" (concat [(move-to start)]
                       (map bezier-to-relative curve)))))

(defn points [^js/Object path n]
  (let [length (.getTotalLength path)
        n (if (= n :length)
            (-> length
                Math/floor
                inc)
            n)]
    (mapv (fn [i]
            (let [x (-> length (* i) (/ (dec n)))
                  p (.getPointAtLength path x)]
              (v/v (.-x p) (.-y p)))) (range n))))

(defn clean-path [d]
  (s/replace d #"l *0 *[, ] *0" ""))

(defn new-path [d]
  (->> d
       clean-path
       (new svg-path-properties/svgPathProperties)))
