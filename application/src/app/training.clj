(ns app.training
  (:require
   [app.file                :as file]
   [mikera.image.core       :as image-core]
   [cortex.experiment.util  :as util]
   [cortex.experiment.train :as train]
   [cortex.nn.layers        :as layers]
   [cortex.nn.network       :as network]))

(defn description
  [categories]
  [(layers/input 52 52 1 :id :data)
   (layers/convolutional 5 0 1 20)
   (layers/relu)
   (layers/max-pooling 2 0 2)
   (layers/dropout 0.9)
   (layers/convolutional 5 0 1 50)
   (layers/relu)
   (layers/max-pooling 2 0 2)
   (layers/batch-normalization)
   (layers/linear 1024)
   (layers/relu :center-loss
                {:label-indexes        {:stream :labels}
                 :label-inverse-counts {:stream :labels}
                 :labels               {:stream :labels}
                 :alpha  0.9
                 :lambda 1e-4})
   (layers/dropout 0.5)
   (layers/linear (count categories))
   (layers/softmax :id :labels)])

(defn make-mapping
  [categories]
  {:class-name->index  (zipmap categories (range))
   :index->class-index (zipmap (range) categories)})

(defn start
  [training-directory validation-directory]
  (let [categories (file/folders training-directory)
        mapping    (make-mapping categories)]
    (train/train-n
     (network/linear-network (description categories))
     (util/infinite-class-balanced-dataset
      (util/create-dataset-from-folder training-directory mapping :image-aug-fn nil))
     (util/create-dataset-from-folder validation-directory mapping)
     :batch-size  100
     :epoch-count 2)))

(comment
  (start "dataset-processed/training"
         "dataset-processed/validation"))
