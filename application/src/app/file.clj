(ns app.file
  (:require
   [clojure.java.io :as io]))

(def make-parents io/make-parents)

(defn exists?
  [path]
  (.exists (io/file path)))

(defn files
  [directory]
  (->> directory
       io/file
       file-seq
       (filter #(.isFile %))))

(defn folders
  [directory]
  (map
   (fn [file]
     (.getName file))
   (.listFiles (io/file directory))))

