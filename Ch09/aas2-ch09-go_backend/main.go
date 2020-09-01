package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
)

type ReqStruct struct{
	Code string `json:"code"`
}

type ResStruct struct{
	Message string `json:"message"`
}


func greeter(w http.ResponseWriter, req *http.Request){
	fmt.Fprintf(w,"Hello!")
}

func secret(w http.ResponseWriter, req *http.Request){
	if req.Method == http.MethodPost{
		var jsonData ReqStruct
		if err := json.NewDecoder(req.Body).Decode(&jsonData); err != nil{
			http.Error(w,err.Error(),http.StatusInternalServerError)
			return
		}
		if jsonData.Code == "gekko"{
			response := &ResStruct{Message: "Blue Horseshoe Loves Anacott Steel"}
			if err := json.NewEncoder(w).Encode(response); err != nil{
				http.Error(w,err.Error(),http.StatusInternalServerError)
				return
			}
		} else {
			response := &ResStruct{Message: "Wrong code, the SEC is on the way to you now."}
			if err := json.NewEncoder(w).Encode(response); err != nil{
				http.Error(w,err.Error(),http.StatusInternalServerError)
				return
			}
		}
	}
}

func main(){
	http.HandleFunc("/greeting",greeter)
	http.HandleFunc("/secret",secret)
	log.Fatal(http.ListenAndServeTLS(":8443","fullchain.pem","privkey.pem",nil))
}