(ns app.testing
  (:require
   [app.file             :as file]
   [cortex.util          :as util]
   [cortex.nn.execute    :as execute]
   [mikera.image.core    :as i]
   [think.image.patch    :as patch]
   [clojure.java.io      :as io]
   [clojure.string       :as string]
   [mikera.image.filters :as filters]))

(defn make-observation
  [file]
  {:labels ["test"]
   :data   (patch/image->patch (i/load-image file) :datatype :float :colorspace :gray)})

(defn answer
  [nippy file]
  (execute/run nippy (into-array [(make-observation file)])))

(defn index->class-name
  [n]
  (nth ["covid" "non_covid"] n))

(defn prediction
  [file nippy]
  (-> (answer nippy file) first :labels util/max-index index->class-name))

(defn probability
  [nippy category files]
  (loop [[current & other :as values] files success 0]
    (if (seq values)
      (if (= category (prediction current nippy))
        (recur other (inc success))
        (recur other success))
      (float (/ success (count files))))))

(defn start
  [testing-directory]
  (let [nippy (util/read-nippy-file "trained-network.nippy")]
    (map (fn [category]
           {:category category
            :value    (probability nippy category (file/files (str testing-directory "/" category)))})
         (file/folders testing-directory))))

(comment
  (def res 
    (doall
     (start "dataset-processed/testing"))))
