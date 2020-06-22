(ns main
  (:require [reagent.core :as r]
            [cljs.core.async :refer (chan put! <! go go-loop timeout)]
            ))

(defonce counter (r/atom 0))

;;(def username (r/atom ""))
;;(def password (r/atom ""))

(def event-queue (chan))

(go-loop [[event payload] (<! event-queue)]
  (case event
    :inc (swap! counter #(+ % payload))
    :dec (swap! counter #(- % payload))
    :login (prn payload))
  (recur (<! event-queue)))

;; ------------ Components -------------------

(defn input-box [type label var]
  [:div.input-box {:class "w-1/2"}
   [:label.p-2 label]
   [:input {:on-change #(reset! var (-> % .-target .-value))
            :type type}]])

(defn login-box []
  (let [username (r/atom "")
        password (r/atom "")]
    [:div
     [input-box "text" "Username: " username]
     [input-box "password" "Password: " password]
     [:button.btn-blue
      {:on-click #(put! event-queue [:login [@username @password]])}
      "Press me"]
     ]))

(defn navbar []
  [:div.flex.flex-row-reverse.bg-black.w-full.text-white.p-4.mb-4
   [:a.nav-item {:href "#"} "HOME"]
   [:a.nav-item {:href "#about"} "ABOUT"]
   [:a.nav-item {:href "#help"} "HELP"]])

;; ------------------ Pages ----------------------

(defn about-page []
  [:div
   [navbar]
   [:h1.text-4xl.font-bold "This about page"]])


(defn help-page []
  [:div
   [navbar]
   [:h1.text-4xl.font-bold "This help page"]])


(defn main-page []
  [:div
   [navbar]
   [:h1 "This is a component"]
   [:p "Hello"]
   [:p.text-2xl {:class (if (< @counter 10)
                          "bg-green-400"
                          "bg-red-400")
                 :on-click #(put! event-queue [:inc 1])
                 } @counter]
   ;;[:ol (for [item (range @counter)] [:li item])]
   [login-box]
   ])

;; ----------------- Utilities --------------------

(defn mount [c]
  (r/render-component [c] (.getElementById js/document "app"))
  )

(def routes
  {"#about" about-page
   "#help" help-page
   "" main-page
   "default" about-page})

(defn handle-routes [routes event]
  (let [loc (.-location.hash js/window)
        newpage (get routes loc (get routes "default"))]
    (.history.replaceState js/window {} nil loc)
    (print newpage)
    (mount newpage)))

(defn setup-router [routes]
  (.addEventListener js/window "hashchange" #(handle-routes routes %))
  (handle-routes routes nil))

(defn reload! []
  (setup-router routes)
  (mount main-page)
  (print "Hello reload!"))

(defn main! []
  (setup-router routes)
  (mount main-page)
  (print "Hello Main"))
