package main

import (
	"bytes"
	"crypto/rand"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/binary"
	"encoding/json"
	"encoding/pem"
	"fmt"
	"io"
	"log"
	"net/http"
	"strings"
	"time"

	jose "github.com/square/go-jose"
)

type Resp struct {
	JWSResult string `json:"jws_result"`
	Action    string `json:"action"`
}

func buildCert(data string) string {
	out := new(bytes.Buffer)
	ctr := 0
	out.WriteString("-----BEGIN CERTIFICATE-----\n")
	for x := 0; x < len(data); x++ {
		if ctr == 64 {
			out.WriteRune('\n')
			ctr = 0
		}
		out.WriteByte(data[x])
		ctr++
	}
	out.WriteString("\n-----END CERTIFICATE-----")
	return out.String()
}

func (r *Resp) verify() ([]byte, error){
	certs := strings.Split(r.JWSResult, ".")[0] + "=="
	out, err := base64.StdEncoding.DecodeString(certs)
	if err != nil {
		return nil, err
	}
	var certObj map[string]interface{}
	if err := json.Unmarshal(out, &certObj); err != nil {
		return nil, err
	}
	pemCert := buildCert(certObj["x5c"].([]interface{})[0].(string))
	block, _ := pem.Decode([]byte(pemCert))
	derCert, err := x509.ParseCertificate(block.Bytes)
	if err != nil {
		return nil, err
	}
	object, err := jose.ParseSigned(r.JWSResult)
	if err != nil {
		return nil, err
	}
	oout, err := object.Verify(derCert.PublicKey)
	if err != nil {
		return nil, err
	}
	return oout,nil
}

func (r *Resp) getDecodedResult(payload []byte) interface{} {
	var obj interface{}
	if err := json.Unmarshal(payload, &obj); err != nil {
		return nil
	}
	return obj
}

func generateNonce(w http.ResponseWriter, req *http.Request) {
	// Here we do minimal error checking and do not bother whether we get a GET or POST
	//fmt.Printf("%+v\n",req)
	rbytes := make([]byte, 8)
	ctime := make([]byte, 8)
	binary.PutUvarint(ctime, uint64(time.Now().Unix()))
	_, err := io.ReadFull(rand.Reader, rbytes)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}
	hash := sha256.New()
	hash.Write(rbytes)
	hash.Write(ctime)
	fmt.Fprintf(w, `{"nonce":"%s"}`, base64.StdEncoding.EncodeToString(hash.Sum(nil)))

}

func validate(w http.ResponseWriter, req *http.Request) {
	var response Resp
	if err := json.NewDecoder(req.Body).Decode(&response); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	payload, err := response.verify()
	if err != nil{
		fmt.Println("Verification Failed")
		fmt.Fprintf(w, `{"validation":false}`)
	}

	jwsResult := response.getDecodedResult(payload).(map[string]interface{})
	ctw := jwsResult["ctsProfileMatch"].(bool)
	bas := jwsResult["basicIntegrity"].(bool)

	if !ctw || !bas {
		fmt.Println("Verification Failed")
		fmt.Fprintf(w, `{"validation":false}`)
	} else {
		fmt.Println("Verification Succeeded")
		fmt.Fprintf(w, `{"validation":true}`)
	}
}

func main() {

	http.HandleFunc("/nonce", generateNonce)
	http.HandleFunc("/validate", validate)

	log.Fatal(http.ListenAndServeTLS(":8443", "fullchain.pem", "privkey.pem", nil))

}
