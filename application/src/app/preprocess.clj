(ns app.preprocess
  (:require
   [app.file :as file]

   [mikera.image.filters :as image-filters]
   [mikera.image.core    :as image-core]))


(defn processing
  [file]
  (-> (image-core/load-image file)
      ((image-filters/grayscale))
      (image-core/resize 52 52)))

(defn upload
  [input-path file-name file]
  (let [path (str input-path "/" file-name)]
    (when-not (file/exists? path)
      (file/make-parents path)
      (-> (processing file)
          (image-core/save path)))))

(defn fragmentation
  [input-path files]
  (doall
   (pmap
    (fn [index file]
      (upload input-path (str index ".png") file))
    (iterate inc 0)
    files)))

(defn source-processing
  [output-path input-path]
  (map
   (fn [category]
     (let [category-files  (-> (str output-path "/" category) file/files shuffle)
           splitting-files (partition (int (/ (count category-files) 5)) category-files)]
       (fragmentation (str input-path "/validation/" category) (->> splitting-files first))
       (fragmentation (str input-path "/testing/"    category) (->> splitting-files second))
       (fragmentation (str input-path "/training/"   category) (->> splitting-files nnext (apply concat)))))
   (file/folders output-path)))

(comment
  (doall
   (source-processing "dataset" "dataset-processed")))
