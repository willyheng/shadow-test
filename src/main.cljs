(ns main
  (:require [reagent.core :as r]
            [cljs.core.async :refer (chan put! <! go go-loop timeout)]
            ))

(defonce counter (r/atom 0))

(def username (r/atom ""))

(def event-queue (chan))

(go-loop [[event payload] (<! event-queue)]
  (case event
    :inc (swap! counter #(+ % payload))
    :dec (swap! counter #(- % payload)))
  (recur (<! event-queue)))

(defn main-component []
  [:div 
   [:h1 "This is a component"]
   [:p "Hello"]
   [:p.text-2xl {:class (if (< @counter 10)
                          "bg-green-400"
                          "bg-red-400")
                 :on-click #(put! event-queue [:inc 1])
                 } @counter]
   ;;[:ol (for [item (range @counter)] [:li item])]
   [:div.m-2.p-1.border-2 {:class "w-1/2"}
    [:label.p-2 "Username"]
    [:input {:on-change #(reset! username (-> % .-target .-value))
             :type "text"}]]
   ])



(defn mount [c]
  (r/render-component [c] (.getElementById js/document "app"))
  )

(defn reload! []
  (mount main-component)
  (print "Hello reload!"))

(defn main! []
  (mount main-component)
  (print "Hello Main"))
